package com.netifera.platform.internal.channel.tcplisten;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.netifera.platform.api.dispatcher.IServerDispatcher;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.channel.socket.TCPSocketTransport;
import com.netifera.platform.channel.tcplisten.ITCPListenServer;


public class TCPListenChannelServer implements ITCPListenServer {
	private static final int PORT_UNINITIALIZED = -1;
	private int listenPort = PORT_UNINITIALIZED;
	private ServerSocket listenSocket;

	private final Thread acceptThread;
	private final ILogger logger;
	private IServerDispatcher serverDispatcher;
	
	public  TCPListenChannelServer(IServerDispatcher serverDispatcher, ILogger logger) {
		acceptThread = new Thread(createAcceptLoopRunnable());
		this.serverDispatcher = serverDispatcher;
		this.logger = logger;
	}
	
	private Runnable createAcceptLoopRunnable() {
		return new Runnable() {
			public void run() {
				runAcceptLoop();
			}
		};
	}
	
	private void runAcceptLoop()  {
		try {
			// Repeat until exception
			while(true) { acceptSingleConnection(); }
		} catch (IOException e) {
			logger.error("IOException accepting socket connection", e);
		} catch(InterruptedException e) {
			// Normal exit, nothing to do here
			return;
		}
        
	}
	
	private void acceptSingleConnection() throws InterruptedException, IOException {
		
		final Socket newConnection = listenSocket.accept();
		
        logger.info("Connection from " + newConnection.getInetAddress().getHostAddress());                                                                                              
        handleNewConnection(newConnection);
	}
	
	private void handleNewConnection(Socket socket) throws IOException {
		serverDispatcher.registerNewConnection(TCPSocketTransport.create(socket));
	}
	

	public void startListening() throws IOException {
		if(listenPort == PORT_UNINITIALIZED) {
			throw new IOException("TCP Listen Channel Server has not been configured with a port to listen on");
		}
		
		listenSocket = new ServerSocket();
		listenSocket.setReuseAddress(true);
		listenSocket.bind(new InetSocketAddress(listenPort));
		acceptThread.start();
	}

	public void stopListening() {
		acceptThread.interrupt();		
	}

	public void setListenPort(int port) {
		if(port < 1 || port > 0xffff) {
			throw new IllegalArgumentException("Listen port must be in the range 1-65535 inclusive");
		}
		this.listenPort = port;		
	}


}
