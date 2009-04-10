package com.netifera.platform.net.packets.decoders;

import com.netifera.platform.net.packets.PacketException;

public class PacketDecodeException extends PacketException {

	private static final long serialVersionUID = 1L;
	public PacketDecodeException(String message) {
		super(message);
	}
	public PacketDecodeException(Throwable cause) {
		super(cause);
	}
}
