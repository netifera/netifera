package com.netifera.platform.net.sniffing;

import com.netifera.platform.net.packets.IPacketHeader;


public interface IPacketSniffer<T extends IPacketHeader> {
	void handlePacket(T packet, IPacketContext ctx);
}
