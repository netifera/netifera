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
	private InetSocketAddress listenAddress;
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
		if(listenAddress == null) {			
			throw new IOException("TCP Listen Channel Server has not been configured with an address to listen on");
		}
		
		listenSocket = new ServerSocket();
		listenSocket.setReuseAddress(true);
		listenSocket.bind(listenAddress);
		acceptThread.start();
	}

	public void stopListening() {
		acceptThread.interrupt();		
	}

	public void setListenAddress(InetSocketAddress address) {
		this.listenAddress = address;
	}
	


}
