package com.netifera.platform.util.locators;

import java.io.Serializable;
import java.net.InetSocketAddress;

import com.netifera.platform.util.addresses.inet.InternetAddress;

public interface ISocketLocator extends Serializable {
	InternetAddress getAddress();
	int getPort();
	String getProtocol();
	InetSocketAddress toInetSocketAddress();
}
