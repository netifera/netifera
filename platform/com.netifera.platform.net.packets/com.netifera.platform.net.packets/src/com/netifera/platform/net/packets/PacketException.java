package com.netifera.platform.net.packets;

@SuppressWarnings("serial")
public class PacketException extends RuntimeException {
	public PacketException(String message) {
		super(message);
	}
	
	public PacketException(Throwable cause) {
		super(cause);
	}
}
