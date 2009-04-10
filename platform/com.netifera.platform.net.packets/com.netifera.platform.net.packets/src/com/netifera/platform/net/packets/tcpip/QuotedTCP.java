package com.netifera.platform.net.packets.tcpip;

import com.netifera.platform.net.packets.AbstractPacket;

public class QuotedTCP extends AbstractPacket {
	int sourcePort;
	int destinationPort;
	
	@Override
	protected int minimumHeaderLength() {
		return 8;
	}

	@Override
	protected void unpackHeader() {
		sourcePort = unpack16();
		destinationPort = unpack16();
	}

	@Override
	protected void packHeader() {
		pack16(sourcePort);
		pack16(destinationPort);
	}
	
	public int getSourcePort() {
		return sourcePort;
	}
	
	public int getDestinationPort() {
		return destinationPort;
	}
	
	@Override
	public String toString() {
		return "TCP " + sourcePort + " -> " + destinationPort;
	}
}
