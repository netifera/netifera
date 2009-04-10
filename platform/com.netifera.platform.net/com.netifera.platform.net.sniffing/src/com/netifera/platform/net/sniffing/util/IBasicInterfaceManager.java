package com.netifera.platform.net.sniffing.util;

import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.link.ARP;
import com.netifera.platform.net.packets.tcpip.IPv4;
import com.netifera.platform.net.packets.tcpip.IPv6;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.IPacketSnifferHandle;
import com.netifera.platform.net.sniffing.stream.IBlockSniffer;
import com.netifera.platform.net.sniffing.stream.IBlockSnifferHandle;
import com.netifera.platform.net.sniffing.stream.IStreamSniffer;
import com.netifera.platform.net.sniffing.stream.IStreamSnifferHandle;

public interface IBasicInterfaceManager {
	IPacketSnifferHandle<IPacketHeader> createRawHandle(IPacketFilter filter, IPacketSniffer<IPacketHeader> sniffer);
	IPacketSnifferHandle<ARP> createArpHandle(IPacketFilter filter, IPacketSniffer<ARP> sniffer);
	IPacketSnifferHandle<IPv4> createIPv4Handle(IPacketFilter filter, IPacketSniffer<IPv4> sniffer);
	IPacketSnifferHandle<IPv6> createIPv6Handle(IPacketFilter filter, IPacketSniffer<IPv6> sniffer);
	IStreamSnifferHandle createTCPStreamHandle(IPacketFilter filter, IStreamSniffer sniffer);
	IBlockSnifferHandle createTCPBlockHandle(IPacketFilter filter, IBlockSniffer sniffer);
	void dispose();
}
