package com.netifera.platform.util.locators;

import java.net.InetSocketAddress;

import com.netifera.platform.util.addresses.inet.InternetAddress;

public class UDPSocketLocator implements ISocketLocator {
	private static final long serialVersionUID = -8153031721220121824L;
	
	private final InternetAddress address;
	private final int port;
	
	public UDPSocketLocator(InternetAddress address, int port) {
		this.address = address;
		this.port = port;
	}
	
	public InternetAddress getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getProtocol() {
		return "udp";
	}
	
	public InetSocketAddress toInetSocketAddress() {
		return new InetSocketAddress(address.toInetAddress(), port);
	}
	
	/*
	static UDPSocketLocator fromInetSocketAddress(InetSocketAddress sockaddr) {
		return new UDPSocketLocator(InternetAddress.fromInetAddress(
				sockaddr.getAddress()), sockaddr.getPort());
	}
	*/
	
	@Override
	public String toString() {
		return address.toStringLiteral() + ":" + Integer.toString(port)
			+ "/udp";
	}
}
