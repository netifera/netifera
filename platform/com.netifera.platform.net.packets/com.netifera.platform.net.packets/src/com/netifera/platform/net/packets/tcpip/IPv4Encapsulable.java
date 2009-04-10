package com.netifera.platform.net.packets.tcpip;

import com.netifera.platform.net.packets.IPacketHeader;

public interface IPv4Encapsulable extends IPacketHeader {
	public int protocolOverIPv4();
}
