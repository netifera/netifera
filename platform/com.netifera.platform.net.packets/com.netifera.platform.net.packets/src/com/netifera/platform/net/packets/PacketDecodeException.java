package com.netifera.platform.net.packets;

@SuppressWarnings("serial")
public class PacketDecodeException extends PacketException {
	public PacketDecodeException(String message) {
		super(message);
	}
	public PacketDecodeException(Throwable cause) {
		super(cause);
	}
}
