package com.netifera.platform.net.sniffing;

import java.util.Collection;

import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.link.ARP;
import com.netifera.platform.net.packets.tcpip.IPv4;
import com.netifera.platform.net.packets.tcpip.IPv6;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.stream.IBlockSniffer;
import com.netifera.platform.net.sniffing.stream.IBlockSnifferHandle;
import com.netifera.platform.net.sniffing.stream.IStreamSniffer;
import com.netifera.platform.net.sniffing.stream.IStreamSnifferHandle;

public interface ISniffingEngineService {
	Collection<ICaptureInterface> getInterfaces();
	
	ISnifferHandle createRawHandle(ICaptureInterface iface,
			IPacketFilter filter, IPacketSniffer<IPacketHeader> sniffer);
	
	ISnifferHandle createArpHandle(ICaptureInterface iface,
			IPacketFilter filter, IPacketSniffer<ARP> sniffer);
	
	ISnifferHandle createIPv4Handle(ICaptureInterface iface,
			IPacketFilter filter, IPacketSniffer<IPv4> sniffer);
	
	ISnifferHandle createIPv6Handle(ICaptureInterface iface,
			IPacketFilter filter, IPacketSniffer<IPv6> sniffer);
	
	IStreamSnifferHandle createTcpStreamHandle(ICaptureInterface iface,
			IPacketFilter filter, IStreamSniffer sniffer);
	
	IBlockSnifferHandle createTcpBlockHandle(ICaptureInterface iface,
			IPacketFilter filter, IBlockSniffer sniffer);
	
	ICaptureFileInterface createCaptureFileInterface(String path);
	
	int getSnaplen();
	int getTimeout();
	boolean getPromiscuous();

}
