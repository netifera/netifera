package com.netifera.platform.internal.channel.tcpconnect;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.netifera.platform.api.channels.IChannelConnectProgress;
import com.netifera.platform.api.dispatcher.IClientDispatcher;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.channel.socket.TCPSocketTransport;
import com.netifera.platform.channel.tcpconnect.ITCPConnectConnecter;

public class TCPConnectConnecter implements ITCPConnectConnecter {

	private final IClientDispatcher dispatcher;
	private final ILogger logger;
	private final Thread acceptThread;
	private volatile IChannelConnectProgress progress;
	private ServerSocket listenSocket;
	private int listenPort;
	
	public TCPConnectConnecter(IClientDispatcher dispatcher, ILogger logger) {
		this.dispatcher = dispatcher;
		this.logger = logger;
		acceptThread = new Thread(createAcceptRunnable());
	}
	
	public void setConnectbackPort(int port) {
		listenPort = port;		
	}

	public void abortConnect() {
		acceptThread.interrupt();		
	}

	public void connect(IChannelConnectProgress progress) {
		this.progress = progress;
		try {
			startListening();
			acceptThread.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void startListening() throws IOException {
		listenSocket = new ServerSocket();
		listenSocket.setReuseAddress(true);
		listenSocket.bind(new InetSocketAddress(listenPort));
	}
	
	private Runnable createAcceptRunnable() {
		return new Runnable() {

			public void run() {
				try {
					runAccept();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		};
	}
	
	private void runAccept() throws IOException {
		final Socket newSocket = listenSocket.accept();
		listenSocket.close();
		logger.info("Connection from " + newSocket.getInetAddress().getHostAddress());
		final IMessenger messenger = dispatcher.createMessenger(TCPSocketTransport.create(newSocket));
		if(progress == null) {
			
		}
		progress.connectCompleted(messenger);
	}

}
