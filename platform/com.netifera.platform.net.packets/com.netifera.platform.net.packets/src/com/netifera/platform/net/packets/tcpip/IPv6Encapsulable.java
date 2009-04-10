package com.netifera.platform.net.packets.tcpip;

import com.netifera.platform.net.packets.IPacketHeader;

public interface IPv6Encapsulable extends IPacketHeader {
	public int protocolOverIPv6();
}
