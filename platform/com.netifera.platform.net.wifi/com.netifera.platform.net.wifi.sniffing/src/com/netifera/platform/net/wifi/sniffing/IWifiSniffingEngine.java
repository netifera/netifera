package com.netifera.platform.net.wifi.sniffing;

import java.util.Collection;

import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.ISnifferHandle;
import com.netifera.platform.net.sniffing.util.ISniffingEngineEx;
import com.netifera.platform.net.wifi.packets.WiFiFrame;
import com.netifera.platform.net.wifi.pcap.IWirelessCaptureInterface;

public interface IWifiSniffingEngine extends ISniffingEngineEx {
	
	Collection<IWirelessCaptureInterface> getWifiInterfaces();

	ISnifferHandle createWifiHandle(IWirelessCaptureInterface iface,
			IPacketFilter filter, IPacketSniffer<WiFiFrame> sniffer);
}
