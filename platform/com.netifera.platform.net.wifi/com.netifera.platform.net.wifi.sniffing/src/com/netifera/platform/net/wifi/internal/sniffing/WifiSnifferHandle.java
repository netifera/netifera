package com.netifera.platform.net.wifi.internal.sniffing;

import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.IPacketSnifferHandle;
import com.netifera.platform.net.wifi.packets.WiFiFrame;

public class WifiSnifferHandle implements IPacketSnifferHandle<WiFiFrame>{

	private final WifiFrameManager manager;
	private final IPacketSniffer<WiFiFrame> sniffer;
	private final IPacketFilter filter;
	
	private Object defaultTag;
	
	public WifiSnifferHandle(WifiFrameManager manager, IPacketFilter filter, IPacketSniffer<WiFiFrame> sniffer) {
		this.manager = manager;
		this.filter = filter;
		this.sniffer = sniffer;
	}
	
	public IPacketFilter getFilter() {
		return filter;
	}

	public IPacketSniffer<WiFiFrame> getSniffer() {
		return sniffer;
	}

	public ICaptureInterface getInterface() {
		return manager.getInterface();
	}

	public void register() {
		manager.registerSniffer(this);		
	}

	public void unregister() {
		manager.unregisterSniffer(this);		
	}

	public void setHighPriority() {
		// Do nothing
		
	}

	public Object getDefaultTag() {
		return defaultTag;
	}

	public void setDefaultTag(Object tag) {
		this.defaultTag = tag;		
	}

}
