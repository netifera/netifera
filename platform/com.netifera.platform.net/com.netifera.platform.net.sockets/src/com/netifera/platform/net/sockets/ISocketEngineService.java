package com.netifera.platform.net.sockets;

import java.io.IOException;


public interface ISocketEngineService {
	
	/** The limit for simultaneously connecting TCP sockets */
	void setMaxConnectingSockets(int limit);
	int getMaxConnectingSockets();
	
	/** The limit for open sockets (including connecting sockets) */
	void setMaxOpenSockets(int limit);
	int getMaxOpenSockets();

	TCPChannel openTCP() throws IOException;
	UDPChannel openUDP() throws IOException;
}
