package com.netifera.platform.net.internal.sniffing.stream;

import com.netifera.platform.net.packets.tcpip.IP;
import com.netifera.platform.net.packets.tcpip.TCP;
import com.netifera.platform.net.sniffing.stream.ISessionKey;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class TCPSessionKey implements ISessionKey {
	/* Immutable! */
	private final InternetAddress clientAddress;
	private final InternetAddress serverAddress;
	private final int clientPort;
	private final int serverPort;
	private final boolean isSYN;
	
	public TCPSessionKey(IP ip, TCP tcp) {
		final InternetAddress sourceAddress = ip.getSourceAddress();
		final InternetAddress destinationAddress = ip.getDestinationAddress();
		final int sourcePort = tcp.getSourcePort();
		final int destinationPort = tcp.getDestinationPort();
		
		/*
		 * The following logic is used to decide which side of a newly
		 * discovered connection is the client and which side is the
		 * server.  The first rule that matches is used:
		 * 
		 * 1) SYN + ACK flags both set        --> Server to Client
		 * 2) SYN flag set (but not ACK)      --> Client to Server
		 * 3) Source Port < Destination Port  --> Server to Client
		 * 4) Otherwise:                      --> Client to Server
		 */
		if((tcp.getSYN() && tcp.getACK()) ||
				(!tcp.getSYN() && (destinationPort > sourcePort))) {
			clientAddress = destinationAddress;
			serverAddress = sourceAddress;
			clientPort = destinationPort;
			serverPort = sourcePort;
		} else {
			clientAddress = sourceAddress;
			serverAddress = destinationAddress;
			clientPort = sourcePort;
			serverPort = destinationPort;
		}
		
		isSYN = tcp.getSYN();
	}
	
	public InternetAddress getServerAddress() {
		return serverAddress;
	}
	
	public InternetAddress getClientAddress() {
		return clientAddress;
	}
	
	public int getServerPort() {
		return serverPort;
	}
	
	public int getClientPort() {
		return clientPort;
	}
	
	public boolean isSYN() {
		return isSYN;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof TCPSessionKey)) {
			return false;
		}
		
		TCPSessionKey key = (TCPSessionKey) o;
		if( (this.clientPort == key.clientPort) &&
				(this.serverPort == key.serverPort) &&
				(this.clientAddress.equals(key.clientAddress)) &&
				(this.serverAddress.equals(key.serverAddress))
			) {
			return true;
		}
		
		if( (this.clientPort == key.serverPort) &&
				(this.serverPort == key.clientPort) &&
				(this.clientAddress.equals(key.serverAddress)) &&
				(this.serverAddress.equals(key.clientAddress))
			) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		/* Include only the address of the lowest port.  If the ports are the same, don't include an address */
		if(clientPort < serverPort) {
			return ((clientPort << 16) | serverPort) ^ clientAddress.hashCode();
		} else if (clientPort > serverPort) {
			return ((serverPort << 16) | clientPort) ^ serverAddress.hashCode();
		} else {
			return ((serverPort << 16) | clientPort);
		}
	}
	
	@Override
	public String toString() {
		return clientAddress.toString() + ":" + clientPort +
	 		" -> " + serverAddress + ":" + serverPort;
	}
	
	public boolean isClientToServer(IP packet) {
		return clientAddress.equals(packet.getSourceAddress()) &&
			clientPort == ((TCP)packet.getNextHeader()).getSourcePort();
		
	}
}
