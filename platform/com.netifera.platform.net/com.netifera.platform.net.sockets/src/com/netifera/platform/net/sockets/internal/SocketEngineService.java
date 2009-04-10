package com.netifera.platform.net.sockets.internal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.sockets.AsynchronousSelectableChannel;
import com.netifera.platform.net.sockets.CompletionHandler;
import com.netifera.platform.net.sockets.ISocketEngineService;
import com.netifera.platform.net.sockets.TCPChannel;
import com.netifera.platform.net.sockets.UDPChannel;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.locators.TCPSocketLocator;
import com.netifera.platform.util.locators.UDPSocketLocator;

public class SocketEngineService implements ISocketEngineService {

	/** The limit for outstanding incomplete socket connections */
	private int maxConnectingSockets = 100;

	/** The limit for open sockets (including connecting sockets) */
	private int maxOpenSockets = 200;

	/** Count of currently connecting sockets */
	final private AtomicInteger currentlyConnectingSockets = new AtomicInteger(0);

	/** Count of all open sockets including connecting sockets */
	final private AtomicInteger currentlyOpenSockets = new AtomicInteger(0);

	/** Selector */
	private Selector selector;

	/** Thread for selector loop */
	private Thread selectThread;

	// XXX document please
	final private BlockingQueue<SelectionContext> registrationQueue = new LinkedBlockingQueue<SelectionContext>();

	final private Map<AsynchronousSelectableChannel, SelectionContext> contextMap = Collections.synchronizedMap(new HashMap<AsynchronousSelectableChannel, SelectionContext>());
	
	private ILogger logger;
	
	/**
	 * We use a cached thread pool because the thread resources are bound by the
	 * maximum open socket count.
	 */
	private final ExecutorService executor = Executors.newCachedThreadPool();

	
	public int getMaxConnectingSockets() {
		return maxConnectingSockets;
	}
	
	public void setMaxConnectingSockets(int limit) {
		maxConnectingSockets = limit;
	}

	public int getMaxOpenSockets() {
		return maxOpenSockets;
	}
	
	public void setMaxOpenSockets(int limit) {
		maxOpenSockets = limit;
	}
	
	public TCPChannel openTCP() throws IOException {
		if (selector == null)
			startSelector();

		TCPChannel channel = new TCPChannel(this, SocketChannel.open());
		channel.getWrappedChannel().configureBlocking(false);

		SelectionContext context = new SelectionContext(this, channel, logger);
		registerChannel(channel, context);
		return channel;
	}

	public UDPChannel openUDP() throws IOException {
		if (selector == null)
			startSelector();

		UDPChannel channel = new UDPChannel(this, DatagramChannel.open());
		channel.getWrappedChannel().configureBlocking(false);

		SelectionContext context = new SelectionContext(this, channel, logger);
		registerChannel(channel, context);
		return channel;
	}

	public <A> Future<Void> asynchronousConnect(TCPChannel channel,
			TCPSocketLocator remote,
			long timeout, TimeUnit unit,
			final A attachment, final CompletionHandler<Void, ? super A> handler) throws IOException, InterruptedException {

		if (selector == null)
			startSelector();

		synchronized (this) {
			while (!canConnect())
				this.wait(); // check and handle timeout
			countConnectingSocket();
		}

		final SocketChannel socket = channel.getWrappedChannel();

		/*
		 * Wrap the handler in another completion handler that performs outstanding connection accounting.
		 */
		final CompletionHandler<Void, A> connectCompletion = new CompletionHandler<Void, A>() {

			public void cancelled(A a) {
				handler.cancelled(a);
				countConnectFinished();				
			}

			public void completed(Void result, A a) {
				handler.completed(result, a);
				countConnectFinished();
			}

			public void failed(Throwable exc, A a) {
				handler.failed(exc, a);
				countConnectFinished();
			}
			
		};
		
		long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
		SelectionFuture<Void,? super A> future = new SelectionFuture<Void,A>(connectCompletion, attachment, deadline, logger, new Callable<Void>() {
			public Void call() throws Exception {
				socket.finishConnect();
				return null;
			}
		});

		InetSocketAddress sockaddr = new InetSocketAddress(remote.getAddress().toInetAddress(), remote.getPort());

		socket.configureBlocking(false);
		try {
			socket.connect(sockaddr);
		} catch(IOException e) {
			countConnectFinished();
			throw e;
		}
		
		SelectionContext context = contextMap.get(channel);
		if (context == null) {
			logger.error("context not found on connect() "+channel);
			handler.cancelled(attachment);
			return null;
		}
		context.enqueueConnect(future);
		registrationQueue.add(context);
		selector.wakeup();
		return future;
	}

	public <A> Future<Integer> asynchronousRead(final AsynchronousSelectableChannel channel,
			final ByteBuffer dst, long timeout, TimeUnit unit,
			final A attachment, final CompletionHandler<Integer,? super A> handler) {

		long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
		SelectionFuture<Integer,A> future = new SelectionFuture<Integer,A>(handler, attachment, deadline, logger, new Callable<Integer>() {
			public Integer call() throws Exception {
				Integer count = ((ReadableByteChannel)channel.getWrappedChannel()).read(dst);
				if (count <= 0) throw new ClosedChannelException();
				return count;
			}
		});
		
		SelectionContext context = contextMap.get(channel);
		if (context == null) {
			logger.error("Context not found on read() "+channel);
			handler.cancelled(attachment);
			return null;
		}
//		if (context.reader != null) throw new PendingReadException();
		context.enqueueRead(future);
		registrationQueue.add(context);
		selector.wakeup();

		return future;
	}
	
	public <A> Future<Integer> asynchronousWrite(final AsynchronousSelectableChannel channel,
			final ByteBuffer src,
			long timeout, TimeUnit unit,
			final A attachment, final CompletionHandler<Integer,? super A> handler) {

		long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
		SelectionFuture<Integer,A> future = new SelectionFuture<Integer,A>(handler, attachment, deadline, logger, new Callable<Integer>() {
			public Integer call() throws Exception {
				Integer count = ((WritableByteChannel)channel.getWrappedChannel()).write(src);
				if (count <= 0) throw new ClosedChannelException();
				return count;
			}
		});
		
		SelectionContext context = contextMap.get(channel);
		if (context == null) {
			logger.error("Context not found on write() "+channel);
			handler.cancelled(attachment);
			return null;
		}
//		if (context.reader != null) throw new PendingReadException();
		context.enqueueWrite(future);
		registrationQueue.add(context);
		selector.wakeup();

		return future;
	}

	public <A> Future<UDPSocketLocator> asynchronousReceive(final UDPChannel channel,
			final ByteBuffer dst, long timeout, TimeUnit unit,
			final A attachment, final CompletionHandler<UDPSocketLocator,? super A> handler) {
		
		long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
		SelectionFuture<UDPSocketLocator,A> future = new SelectionFuture<UDPSocketLocator,A>(handler, attachment, deadline, logger, new Callable<UDPSocketLocator>() {
			public UDPSocketLocator call() throws Exception {
				InetSocketAddress address = (InetSocketAddress) channel.getWrappedChannel().receive(dst);
				return new UDPSocketLocator(InternetAddress.fromInetAddress(address.getAddress()),address.getPort());
			}
		});
		
		SelectionContext context = contextMap.get(channel);
		if (context == null) {
			logger.error("Context not found on recv() for "+channel);
			handler.cancelled(attachment);
			return null;
		}
		context.enqueueRead(future);
		registrationQueue.add(context);
		selector.wakeup();

		return future;
	}

	public <A> Future<Integer> asynchronousSend(final UDPChannel channel,
			final ByteBuffer src,
			final UDPSocketLocator target,
			long timeout, TimeUnit unit,
			final A attachment, final CompletionHandler<Integer,? super A> handler) {
		
		long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
		SelectionFuture<Integer,A> future = new SelectionFuture<Integer,A>(handler, attachment, deadline, logger, new Callable<Integer>() {
			public Integer call() throws Exception {
				return channel.getWrappedChannel().send(src, new InetSocketAddress(target.getAddress().toInetAddress(),target.getPort()));
			}
		});
		
		SelectionContext context = contextMap.get(channel);
		if (context == null) {
			logger.error("Context not found on send() "+channel);
			handler.cancelled(attachment);
			return null;
		}
		context.enqueueWrite(future);
		registrationQueue.add(context);
		selector.wakeup();

		return future;
	}

	private void countOpenSocket() {
		currentlyOpenSockets.incrementAndGet();
	}
	
	/**
	 * Count a new open connection by incrementing
	 * <code>currentlyConnectingSockets</code>
	 */
	private void countConnectingSocket() {
		currentlyConnectingSockets.incrementAndGet();
	}

	/**
	 * Count a completed (or failed) connection by decrementing the
	 * <code>currentlyConnectingSockets</code> counter and wake up threads
	 * that may be sleeping while waiting for socket resources to become
	 * available.
	 */
	 private void countConnectFinished() {
		currentlyConnectingSockets.decrementAndGet();
		synchronized (this) {
			this.notifyAll();
		}
	}

	/**
	 * Count a closed socket by decrementing the
	 * <code>currentlyOpenSockets</code> and wake up threads that may be
	 * sleeping while waiting for socket resources to become available.
	 */
	private void countSocketClose() {
		currentlyOpenSockets.decrementAndGet();
		synchronized (this) {
			this.notifyAll();
		}

	}

	/**
	 * Test counters to verify that resources are available to create a new
	 * connecting sockets.
	 * 
	 * @return True if socket resources are below limits, otherwise false.
	 */
	private boolean canConnect() {
		return (currentlyConnectingSockets.get() < maxConnectingSockets)
				&& (currentlyOpenSockets.get() < maxOpenSockets);
	}

	/**
	 * Create {@link Selector} and start select loop thread.
	 * 
	 * @throws IOException
	 *             Error creating Selector.
	 */
	private void startSelector()  {
		assert (selector == null);

		try {
			selector = Selector.open();
		} catch(IOException e) {
			assert logger != null;
			logger.error("I/O error, cannot open selector", e);
			return;
		}

		selectThread = new Thread(new Runnable() {

			public void run() {

				selectLoop();

				try {
					selector.close();
				} catch (IOException e) {
					assert logger != null;
					logger.error("I/O error closing selector", e);
				}
				selector = null;
			}

		});

		selectThread.setDaemon(true);
		selectThread.setName("Socket Connect Engine Selector thread");
		selectThread.start();
	}

	private void registerPending() {
		SelectionContext context;
		while ((context = registrationQueue.poll()) != null)
			context.register();
	}
	
	/**
	 * The select loop multiplexes the connection status of multiple sockets and
	 * detects completed connections and expired connection timeout values.
	 */
	private void selectLoop() {
		long timeout = 0; // wait indefinitely
	
		registerPending();
	
		while (!Thread.interrupted()) {
			if (contextMap.isEmpty() && currentlyOpenSockets.get() == 0 && currentlyConnectingSockets.get() == 0) {
				assert logger != null;
				logger.debug("SocketEngineService clean");
				timeout = 0;
			} else {
//				System.out.println("active contexts: "+contextMap.size()+" selection keys: "+selector.keys().size());
//				System.out.println("open sockets: "+currentlyOpenSockets.get()+" connecting sockets: "+currentlyConnectingSockets.get());
				timeout = Math.max(timeout, 500); // XXX
			}

			try {
				selector.select(timeout);
				assert selector != null;
				if (selector.isOpen() == false) {
					return;
				}
			} catch (IOException e) {
				assert logger != null;
				logger.error("I/O error in Selector#select()", e);
				return;
			}
			
			registerPending();

			for (SelectionKey key : selector.selectedKeys()) {
				SelectionContext context = (SelectionContext)key.attachment();
				try {
					context.testKey(key);
				} catch (CancelledKeyException e) {
					// a selected key is cancelled
					//logger.warning("Cancelled selector key (on selected key)", e);
					// do something about it
					context.close();
				}
			}

			long now = System.currentTimeMillis();
			timeout = Long.MAX_VALUE;
			for (SelectionKey key : selector.keys()) {
				SelectionContext context = (SelectionContext)key.attachment();
				try {
					timeout = Math.min(timeout, context.testTimeOut(key, now));
				} catch (CancelledKeyException e) {
					//logger.warning("Cancelled selector key (when testing timeout of unselected key)", e);
					// do something about it. should close?
				}
			}
			if (timeout == Long.MAX_VALUE) timeout = 0; // 0 means wait indefinitely
		}
	}
	
	/**
	 * Close the socket connect engine. This method is for testing that
	 * resources such as socket handles are correctly freed and should be
	 * removed later.
	 */
	// TODO called by deactivate()
	void close() {
		//selectThread.interrupt();
		
		try {
			selector.close();
		} catch (IOException e) {
			logger.error("I/O error closing selector", e);
		}
		executor.shutdownNow();
	}

	private synchronized void registerChannel(AsynchronousSelectableChannel channel, SelectionContext context) {
		contextMap.put(channel, context);
		countOpenSocket();
	}
	
	public synchronized void unregisterChannel(AsynchronousSelectableChannel channel) {
		if (null != contextMap.remove(channel))
			countSocketClose();
	}
	
/*	boolean hasSelectionContextFor(AsynchronousSelectableChannel channel) {
		return contextMap.containsKey(channel);
	}
*/	
	Selector getSelector() {
		return selector;
	}
	
	public ExecutorService getExecutor() {
		return executor;
	}

	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Socket Engine");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		// FIXME commented: workaround for #159
		//logger = null;
	}
}
