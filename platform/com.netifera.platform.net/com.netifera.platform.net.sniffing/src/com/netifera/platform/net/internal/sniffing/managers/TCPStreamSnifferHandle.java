package com.netifera.platform.net.internal.sniffing.managers;

import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.stream.IStreamSniffer;
import com.netifera.platform.net.sniffing.stream.IStreamSnifferHandle;

public class TCPStreamSnifferHandle implements IStreamSnifferHandle {

	private final IPacketFilter filter;
	private final IStreamSniffer sniffer;
	private final TCPManager manager;
	
	private Object defaultTag;
	
	public TCPStreamSnifferHandle(TCPManager tcpManager,
			IPacketFilter filter, IStreamSniffer sniffer) {
		if(tcpManager == null) {
			throw new IllegalArgumentException("tcp manager is null");
		}
				
		this.filter = filter;
		this.sniffer = sniffer;
		this.manager = tcpManager;
		
	}
	
	public ICaptureInterface getInterface() {
		return manager.getInterface();
	}

	public IPacketFilter getFilter() {
		return filter;
	}
	
	public IStreamSniffer getSniffer() {
		return sniffer;
	}
	
	public void register() {
		manager.registerStreamHandle(this);
	}

	public void unregister() {
		manager.unregisterStreamHandle(this);
	}

	public void setHighPriority() {
		// Do nothing for TCP handles		
	}

	public Object getDefaultTag() {
		return defaultTag;
	}

	public void setDefaultTag(Object tag) {
		this.defaultTag = tag;		
	}
}
