package com.netifera.platform.net.internal.sniffing.managers;

import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.IPacketSnifferHandle;

public class SnifferHandle<T extends IPacketHeader> implements
		IPacketSnifferHandle<T> {
	
	private final IPacketFilter filter;
	private final IPacketSniffer<T> sniffer;
	private final IPacketManager<T> manager;
	
	private Object defaultTag;
	private boolean isHighPriority;
	
	public void register() {
		if(isHighPriority) {
			manager.registerPrioritySniffer(this);
		} else {
			manager.registerSniffer(this);
		}
	}

	public void unregister() {
		manager.unregisterSniffer(this);
	}
	
	public SnifferHandle(IPacketManager<T> manager, IPacketFilter filter,
			IPacketSniffer<T> sniffer) {
		this.manager = manager;
		this.filter = filter;
		this.sniffer = sniffer;
	}
	
	public ICaptureInterface getInterface() {
		return manager.getInterface();
	}
	
	public IPacketFilter getFilter() {
		return filter;
	}
	
	public IPacketSniffer<T> getSniffer() {
		return sniffer;
	}
	
	public void setHighPriority() {
		isHighPriority = true;
	}
	
	public void setDefaultTag(Object tag) {
		this.defaultTag = tag;
	}
	
	public Object getDefaultTag() {
		return defaultTag;
	}
	

}
