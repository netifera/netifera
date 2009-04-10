package com.netifera.platform.internal.channel.tcpconnect;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.netifera.platform.api.dispatcher.IServerDispatcher;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.channel.socket.TCPSocketTransport;
import com.netifera.platform.channel.tcpconnect.ITCPConnectServer;

public class TCPConnectServer implements ITCPConnectServer {

	private IServerDispatcher serverDispatcher;
	private InetSocketAddress connectBackAddress;
	private final Socket socket;
	private final ILogger logger;
	
	public TCPConnectServer(IServerDispatcher serverDispatcher, ILogger logger) {
		this.serverDispatcher = serverDispatcher;
		this.logger = logger;
		this.socket = new Socket();
	}
	
	public void setAddress(InetSocketAddress address) {
		connectBackAddress = address;		
	}

	public void startListening() throws IOException {
		if(connectBackAddress == null) {
			
		}
		// XXX background thread
		logger.info("Connecting back to " + connectBackAddress);
		socket.connect(connectBackAddress);
		serverDispatcher.registerNewConnection(TCPSocketTransport.create(socket));
		
	}

	public void stopListening() {
		// do nothing, interrupt connect thread?
	}

}
