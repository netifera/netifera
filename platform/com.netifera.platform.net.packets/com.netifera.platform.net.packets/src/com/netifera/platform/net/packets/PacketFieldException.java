package com.netifera.platform.net.packets;

public class PacketFieldException extends PacketException {

	private static final long serialVersionUID = 1L;
	
	PacketFieldException(int value) {
		super("Illegal field value: " + value);
	}
	public PacketFieldException(String message) {
		super(message);
	}
}
