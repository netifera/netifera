package com.netifera.platform.net.sockets.internal;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.sockets.AsynchronousSelectableChannel;

/**
 * A <code>SelectionContext</code> object encapsulates all the state of a
 * socket under control of the socket engine
 */
class SelectionContext {
	private final SocketEngineService engine;
	private final AsynchronousSelectableChannel asynchronousChannel;
	private final SelectableChannel socket;
	private final ILogger logger;
	
	private final Queue<SelectionFuture<Void,?>> connectQueue = new ArrayBlockingQueue<SelectionFuture<Void,?>>(100);
	private final Queue<SelectionFuture<?,?>> readQueue = new ArrayBlockingQueue<SelectionFuture<?,?>>(100);
	private final BlockingQueue<SelectionFuture<Integer,?>> writeQueue = new ArrayBlockingQueue<SelectionFuture<Integer,?>>(100);
	
	SelectionContext(SocketEngineService engine, AsynchronousSelectableChannel channel, ILogger logger) {
		this.engine = engine;
		this.asynchronousChannel = channel;
		this.socket = channel.getWrappedChannel();
		this.logger = logger;
	}

	synchronized void enqueueConnect(SelectionFuture<Void,?> future) {
//		System.err.println("enqueue connect "+future+", #"+(connectQueue.size()+1));
		connectQueue.add(future);
	}

	synchronized void enqueueRead(SelectionFuture<?,?> future) {
//		System.err.println("enqueue read "+future+", #"+(readQueue.size()+1));
		readQueue.add(future);
	}
	
	synchronized void enqueueWrite(SelectionFuture<Integer,?> future) {
//		System.err.println("enqueue write "+future+", #"+(writeQueue.size()+1));
		try {
			if(!writeQueue.offer(future, 2, TimeUnit.SECONDS)) {
				logger.warning("Failed to place write on queue for channel: " + asynchronousChannel);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("op:");
		boolean opAdded = false;
		if (connectQueue.peek() != null) {
			opAdded = true;
			sb.append("CONNECT");
		}
		if (readQueue.peek() != null) {
			if (opAdded) {
				sb.append('|');
			}
			opAdded = true;
			sb.append("READ");
		}
		if (writeQueue.peek() != null) {
			if (opAdded) {
				sb.append('|');
			}
			opAdded = true;
			sb.append("WRITE");
		}
		if (!opAdded) {
			sb.append("NONE");
		}
		return sb.toString();
	}
	
	public synchronized int getInterestOps() {
		int interestOps = 0;
		if (connectQueue.peek() != null)
			interestOps |= SelectionKey.OP_CONNECT;
		if (readQueue.peek() != null)
			interestOps |= SelectionKey.OP_READ;
		if (writeQueue.peek() != null)
			interestOps |= SelectionKey.OP_WRITE;
		return interestOps;
	}
	
	synchronized boolean register() {
		boolean registered = false;
		try {
//			System.out.println("register "+socket+" interest ops "+getInterestOps());
//			SelectionKey key = socket.keyFor(engine.getSelector());
//			if (key != null && key.isValid())
//				key.interestOps(getInterestOps());
//			else
			if (getInterestOps() != 0) {
				socket.register(engine.getSelector(), getInterestOps(), this);
				registered = true;
			}
		} catch (ClosedChannelException e) {
			// do nothing
			logger.error("Closed channel? "+socket, e);
		} catch (CancelledKeyException e) {
			logger.error("Canceled key for "+socket, e);
			close(); //XXX
		}
		return registered;
	}
	
	
	/**
	 * Close the specified socket channel and decrease the open socket counter.
	 * 
	 * @param channel
	 *            Socket channel to be closed.
	 */
	public synchronized void close() {
		try {
			asynchronousChannel.close();
		} catch (IOException e) {
			logger.error("I/O error closing socket", e);
		}
	}

	private void handleOperation(SelectionFuture<?,?> future) {
		if (future != null && !future.isCancelled() && !future.isDone())
			engine.getExecutor().execute(future);
	}

	/**
	 * Test the given key to determine if the timeout period has expired. If the
	 * connection has timed out the socket channel is closed, the key is
	 * canceled, and then the {@link IConnectConsumer#failed()} method is called
	 * with a {@link SocketTimeoutException}.
	 * 
	 * @param key
	 *            Key to be tested.
	 * @param now
	 *            Current clock value in milliseconds to compare against connect
	 *            start time.
	 *
	 * @throws SocketTimeoutException 
	 */
	public synchronized long testTimeOut(SelectionKey key, long now) {
		long timeout = Long.MAX_VALUE;
		if (!key.isValid()) {
//			System.err.println("invalid key (timeouted) "+asynchronousChannel);
			key.cancel(); //XXX is this ok???
			return timeout;
		}
		
		if ((key.interestOps() & SelectionKey.OP_CONNECT) != 0) {
			if (connectQueue.peek().testTimeOut(now)) {
				connectQueue.poll();
			} else {
				timeout = Math.min(timeout, connectQueue.peek().getDeadline() - now);
			}
		}
		if ((key.interestOps() & SelectionKey.OP_READ) != 0) {
			if (readQueue.peek().testTimeOut(now)) {
				readQueue.poll();
			} else {
				timeout = Math.min(timeout, readQueue.peek().getDeadline() - now);
			}
		}
		if ((key.interestOps() & SelectionKey.OP_WRITE) != 0) {
			if (writeQueue.peek().testTimeOut(now)) {
				writeQueue.poll();
			} else {
				timeout = Math.min(timeout, writeQueue.peek().getDeadline() - now);
			}
		}
		key.interestOps(getInterestOps());
		if (getInterestOps() == 0)
			key.cancel();
		return timeout;
	}
	
	/**
	 * Tests a given key to determine if it is still valid and has completed (or
	 * failed) a connection. If the key is determined to be connectable, it is
	 * canceled and a new thread is started for calling the connection
	 * callbacks.
	 * 
	 * @param key
	 *            The key to be tested.
	 */
	public synchronized void testKey(SelectionKey key) {
//		System.out.println("test key "+(connectQueue.peek()!=null)+" "+(readQueue.peek()!=null)+" "+(writeQueue.peek()!=null));
		if (!key.isValid()) {
			logger.error("invalid key; closeing "+asynchronousChannel);
			key.cancel();
			this.close();
			return;
		}
		
		boolean connectable = key.isConnectable();
		boolean readable = key.isReadable();
		boolean writable = key.isWritable();
		
		if (connectable) {
			handleOperation(connectQueue.poll());
			key.cancel();
			return;
		}
		if (readable)
			handleOperation(readQueue.poll());
		if (writable)
			handleOperation(writeQueue.poll());
		
		key.interestOps(getInterestOps());
		if (getInterestOps() == 0)
			key.cancel(); //XXX is this ok?
	}
}
