package com.netifera.platform.net.wifi.internal.sniffing;

import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.link.ARP;
import com.netifera.platform.net.packets.tcpip.IPv4;
import com.netifera.platform.net.packets.tcpip.IPv6;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.IPacketSnifferHandle;
import com.netifera.platform.net.sniffing.stream.IBlockSniffer;
import com.netifera.platform.net.sniffing.stream.IBlockSnifferHandle;
import com.netifera.platform.net.sniffing.stream.IStreamSniffer;
import com.netifera.platform.net.sniffing.stream.IStreamSnifferHandle;
import com.netifera.platform.net.sniffing.util.IBasicInterfaceManager;
import com.netifera.platform.net.sniffing.util.ISniffingEngineEx;
import com.netifera.platform.net.wifi.packets.WiFiFrame;
import com.netifera.platform.net.wifi.pcap.IWirelessCaptureInterface;


public class WifiInterfaceManager implements IBasicInterfaceManager {
	
	private final IBasicInterfaceManager basicManager;
	private final WifiFrameManager wifiManager;
	private final IWirelessCaptureInterface captureInterface;
	
	WifiInterfaceManager(IBasicInterfaceManager basicManager, ISniffingEngineEx sniffingEngine, IWirelessCaptureInterface captureInterface) {
	
		this.basicManager = basicManager;
		wifiManager = new WifiFrameManager(sniffingEngine, captureInterface);
		this.captureInterface = captureInterface;
	}
	
	public ICaptureInterface getInterface() {
		return captureInterface;
	}
	
	
	IPacketSnifferHandle<WiFiFrame> createWifiHandle(IPacketFilter filter, IPacketSniffer<WiFiFrame> sniffer) {
		return new WifiSnifferHandle(wifiManager, filter, sniffer);
	}
	


	public IPacketSnifferHandle<ARP> createArpHandle(IPacketFilter filter,
			IPacketSniffer<ARP> sniffer) {
		return basicManager.createArpHandle(filter, sniffer);
	}

	public IPacketSnifferHandle<IPv4> createIPv4Handle(IPacketFilter filter,
			IPacketSniffer<IPv4> sniffer) {
		return basicManager.createIPv4Handle(filter, sniffer);
	}

	public IPacketSnifferHandle<IPv6> createIPv6Handle(IPacketFilter filter,
			IPacketSniffer<IPv6> sniffer) {
		return basicManager.createIPv6Handle(filter, sniffer);
	}

	public IPacketSnifferHandle<IPacketHeader> createRawHandle(
			IPacketFilter filter, IPacketSniffer<IPacketHeader> sniffer) {
		return basicManager.createRawHandle(filter, sniffer);
	}

	public IBlockSnifferHandle createTCPBlockHandle(IPacketFilter filter,
			IBlockSniffer sniffer) {
		return basicManager.createTCPBlockHandle(filter, sniffer);
	}

	public IStreamSnifferHandle createTCPStreamHandle(IPacketFilter filter,
			IStreamSniffer sniffer) {
		return basicManager.createTCPStreamHandle(filter, sniffer);
	}

	public void dispose() {
		basicManager.dispose();		
	}
}
