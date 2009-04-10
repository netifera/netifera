package com.netifera.platform.channel.socket;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ConfigParser {
	public final static int INVALID_PORT = -1;

	private final String prefix;
	
	public ConfigParser(String prefix) {
		this.prefix = prefix;
	}
	/*
	 * channelConfig is expected in the form "tcplisten:a.b.c.d:port"
	 * for example "tcplisten:22.99.7.12:443"
	 */
	public InetSocketAddress configToAddress(String channelConfig) {
		if(!isValidAddressConfig(channelConfig))
			return null;
		
		final InetAddress address = configToInetAddress(channelConfig);
		final int port = configToPort(channelConfig);
		return new InetSocketAddress(address, port);
	}
	
	private boolean isValidAddressConfig(String config) {
		
		try {
			final String[] parts = splitConfig(config);
		
			if(parts[0].equals(prefix) &&
				IPv4Address.isValid(parts[1]) &&
				stringToPort(parts[2]) != INVALID_PORT)
				return true;
			if(IPv4Address.isValid(parts[0]) &&
					stringToPort(parts[1]) != INVALID_PORT)
				return true;
			return false;
		} catch(IllegalArgumentException e) {
			return false;
		}
	}
	
	private InetAddress configToInetAddress(String config) {
		final String[] parts = splitConfig(config);
		final int addressData = IPv4Address.stringParse(parts[1]);
		final IPv4Address address = new IPv4Address(addressData);
		return address.getInetAddress();
	}
	
	public int configToPort(String config) {
		final String[] parts = splitConfig(config);
		final int last = parts.length - 1;
		return stringToPort(parts[last]);
	}
	
	private String[] splitConfig(String config) {
		final String[] parts = config.split(":");
		if(parts.length != 3 && parts.length != 2) {
			throw new IllegalArgumentException();
		} else {
			return parts;
		}
	}
	public int stringToPort(String s) {
		try {
			final int port = Integer.parseInt(s);
			return (isValidPort(port)) ? (port) : (INVALID_PORT);
		} catch(NumberFormatException e) {
			return INVALID_PORT;
		}	
	
	}
	
	private boolean isValidPort(int port) {
		return port > 0 && port <= 65535;
	}

}
