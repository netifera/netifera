package com.netifera.platform.net.sniffing.util;

import com.netifera.platform.net.internal.sniffing.managers.ArpManager;
import com.netifera.platform.net.internal.sniffing.managers.CaptureFileManager;
import com.netifera.platform.net.internal.sniffing.managers.IPv4Manager;
import com.netifera.platform.net.internal.sniffing.managers.IPv6Manager;
import com.netifera.platform.net.internal.sniffing.managers.RawManager;
import com.netifera.platform.net.internal.sniffing.managers.SnifferHandle;
import com.netifera.platform.net.internal.sniffing.managers.TCPBlockSnifferHandle;
import com.netifera.platform.net.internal.sniffing.managers.TCPManager;
import com.netifera.platform.net.internal.sniffing.managers.TCPStreamSnifferHandle;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.link.ARP;
import com.netifera.platform.net.packets.tcpip.IPv4;
import com.netifera.platform.net.packets.tcpip.IPv6;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.pcap.ex.ICaptureInterfaceEx;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.IPacketSnifferHandle;
import com.netifera.platform.net.sniffing.stream.IBlockSniffer;
import com.netifera.platform.net.sniffing.stream.IBlockSnifferHandle;
import com.netifera.platform.net.sniffing.stream.IStreamSniffer;
import com.netifera.platform.net.sniffing.stream.IStreamSnifferHandle;

public class InterfaceManager implements IBasicInterfaceManager {
	
	private final IPacketSource packetSource;
	private final ArpManager arpManager;
	private final IPv4Manager ipv4Manager;
	private final IPv6Manager ipv6Manager;
	private final TCPManager tcpManager;

	public static InterfaceManager createCaptureFileManager(ISniffingEngineEx engine, CaptureFileInterface iface) {
		return new InterfaceManager(new CaptureFileManager(engine, iface));
	}
	
	public static InterfaceManager createRawManager(ISniffingEngineEx engine, ICaptureInterfaceEx iface) {
		return new InterfaceManager(new RawManager(engine, iface));
	}
	
	public static InterfaceManager createBasic(IPacketSource packetSource) {
		return new InterfaceManager(packetSource);
	}
	
	private InterfaceManager(IPacketSource packetSource) {
		this.packetSource = packetSource;
		arpManager = new ArpManager(packetSource);
		ipv4Manager = new IPv4Manager(packetSource);
		ipv6Manager = new IPv6Manager(packetSource);
		tcpManager = new TCPManager(packetSource, ipv4Manager, ipv6Manager);
	}
	
	public ISniffingEngineEx getSniffingEngine() {
		return packetSource.getSniffingEngine();
	}
	
	public ICaptureInterface getInterface() {
		return packetSource.getInterface();
	}
	
	public IPacketSnifferHandle<IPacketHeader> createRawHandle(IPacketFilter filter, IPacketSniffer<IPacketHeader> sniffer) {
		return new SnifferHandle<IPacketHeader>(packetSource, filter, sniffer);
	}
	
	public IPacketSnifferHandle<ARP> createArpHandle(IPacketFilter filter, IPacketSniffer<ARP> sniffer) {
		return new SnifferHandle<ARP>(arpManager, filter, sniffer);
	}
	
	public IPacketSnifferHandle<IPv4> createIPv4Handle(IPacketFilter filter, IPacketSniffer<IPv4> sniffer) {
		return new SnifferHandle<IPv4>(ipv4Manager, filter, sniffer);
	}
	
	public IPacketSnifferHandle<IPv6> createIPv6Handle(IPacketFilter filter, IPacketSniffer<IPv6> sniffer) {
		return new SnifferHandle<IPv6>(ipv6Manager, filter, sniffer);
	}
	
	public IStreamSnifferHandle createTCPStreamHandle(IPacketFilter filter, IStreamSniffer sniffer) {
		return new TCPStreamSnifferHandle(tcpManager, filter, sniffer);
	}
	
	public IBlockSnifferHandle createTCPBlockHandle(IPacketFilter filter, IBlockSniffer sniffer) {
		return new TCPBlockSnifferHandle(tcpManager, filter, sniffer);
	}
	
	public void dispose() {
		// XXX call dispose on each manager
	}
}
