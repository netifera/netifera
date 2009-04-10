package com.netifera.platform.util.locators;

import com.netifera.platform.util.addresses.inet.InternetAddress;

public class SSLSocketLocator extends TCPSocketLocator {
	private static final long serialVersionUID = 6861783658607769609L;

	public SSLSocketLocator(InternetAddress address, int port) {
		super(address, port);
	}
	
	@Override
	public String getProtocol() {
		return "ssl";
	}
}
