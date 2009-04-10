package com.netifera.platform.net.http.service;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpException;
import org.apache.http.ProtocolException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.nio.DefaultClientIOEventDispatch;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.protocol.BufferingHttpClientHandler;
import org.apache.http.nio.protocol.EventListener;
import org.apache.http.nio.protocol.HttpRequestExecutionHandler;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.SessionRequest;
import org.apache.http.nio.reactor.SessionRequestCallback;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestUserAgent;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.http.internal.service.Activator;
import com.netifera.platform.util.locators.TCPSocketLocator;

public class AsynchronousHTTPClient {
	private final TCPSocketLocator locator;
	private final ILogger logger;
	private final ConnectingIOReactor ioReactor;
	private final EventListener connectionsListener;
	private final AtomicInteger connectionsCount = new AtomicInteger(0);

	private class ConnectionsListener implements EventListener {
		public void connectionOpen(final NHttpConnection conn) {
//			connectionsCount.incrementAndGet(); //!!already in the SessionRequestCallback
			logger.debug(info("Connection open"));
			if (connectionsListener != null)
				connectionsListener.connectionOpen(conn);
		}

		public void connectionTimeout(final NHttpConnection conn) {
//			connectionsCount.decrementAndGet();
			logger.debug(info("Connection timed out"));
			if (connectionsListener != null)
				connectionsListener.connectionTimeout(conn);
		}

		public void connectionClosed(final NHttpConnection conn) {
			connectionsCount.decrementAndGet();
			logger.debug(info("Connection closed"));
			if (connectionsListener != null)
				connectionsListener.connectionClosed(conn);
		}

		public void fatalIOException(final IOException ex, final NHttpConnection conn) {
//			connectionsCount.decrementAndGet(); //XXX is this ok?
			logger.error(info("I/O error"), ex);
			if (connectionsListener != null)
				connectionsListener.fatalIOException(ex, conn);
		}

		public void fatalProtocolException(final HttpException ex, final NHttpConnection conn) {
//			connectionsCount.decrementAndGet(); //XXX is this ok?
			if (ex instanceof ProtocolException) {
				logger.error(info("Protocol error") + ": " + ex.getMessage());
			} else {
				logger.debug(debug("HTTP error", conn), ex);
			}
			if (connectionsListener != null)
				connectionsListener.fatalProtocolException(ex, conn);
		}
		
		private String info(String errtype) {
			return errtype + " at " + locator;
		}
		
		private String debug(String errtype, NHttpConnection conn) {
			return info(errtype) + ", count:" + connectionsCount + ", " + conn;
		}
	}

	public AsynchronousHTTPClient(TCPSocketLocator locator, EventListener connectionsListener, HttpRequestExecutionHandler requestHandler) throws IOReactorException {
		// TODO better logging with loggingContext
		this(locator, connectionsListener, requestHandler, Activator.getInstance().getLogManager().getLogger("Asynchronous HTTP Client"));
	}
	
	public AsynchronousHTTPClient(TCPSocketLocator locator, EventListener connectionsListener, HttpRequestExecutionHandler requestHandler, final ILogger logger) throws IOReactorException {
		this.locator = locator;
		this.connectionsListener = connectionsListener;
		this.logger = logger;
		
		HttpParams params = new BasicHttpParams();
		params
				.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
				.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000)
				.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
				.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true)
				.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
				.setParameter(CoreProtocolPNames.USER_AGENT, HTTPClient.DEFAULT_USER_AGENT);

		ioReactor = new DefaultConnectingIOReactor(2, params);

		BasicHttpProcessor httpproc = new BasicHttpProcessor();
		httpproc.addInterceptor(new RequestContent());
// XXX the user should set target host
//		httpproc.addInterceptor(new RequestTargetHost());
		httpproc.addInterceptor(new RequestConnControl());
		httpproc.addInterceptor(new RequestUserAgent());
		httpproc.addInterceptor(new RequestExpectContinue());

		BufferingHttpClientHandler handler = new BufferingHttpClientHandler(
				httpproc, requestHandler, new DefaultConnectionReuseStrategy(),
				params);

		handler.setEventListener(new ConnectionsListener());

		final IOEventDispatch ioEventDispatch = new DefaultClientIOEventDispatch(
				handler, params);

		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					ioReactor.execute(ioEventDispatch);
				} catch (InterruptedIOException ex) {
					//logger.warning("Interrupted");
				} catch (IOException e) {
					logger.error("I/O error", e);
				}
				logger.debug("Shutdown HTTP Client");
			}
		});
		t.setName("HTTP Client on " + locator);
		t.start();
	}

	@Deprecated // XXX could never return on error: infinite loop -> need detailed javadoc
	public void connect() {
		connect(null);
	}
	
	public synchronized void connect(final SessionRequestCallback callback) {
		connectionsCount.incrementAndGet();
		ioReactor.connect(new InetSocketAddress(locator.getAddress()
				.toInetAddress(), locator.getPort()), null, null, new SessionRequestCallback() {
					public void cancelled(SessionRequest request) {
						connectionsCount.decrementAndGet();
						if (callback != null) callback.cancelled(request);
					}
					public void completed(SessionRequest request) {
						if (callback != null) callback.completed(request);
					}
					public void failed(SessionRequest request) {
						connectionsCount.decrementAndGet();
						if (callback != null) callback.failed(request);
					}
					public void timeout(SessionRequest request) {
						connectionsCount.decrementAndGet();
						if (callback != null) callback.timeout(request);
					}
		});
	}
	
	public void shutdown() throws IOException {
		ioReactor.shutdown();
	}
	
	public int getConnectionsCount() {
		return connectionsCount.get();
	}
}
