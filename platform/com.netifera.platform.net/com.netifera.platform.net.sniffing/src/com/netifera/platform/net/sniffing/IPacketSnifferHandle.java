package com.netifera.platform.net.sniffing;

import com.netifera.platform.net.packets.IPacketHeader;

public interface IPacketSnifferHandle <T extends IPacketHeader> extends ISnifferHandle {
	IPacketSniffer<T> getSniffer();
	IPacketFilter getFilter();
}
