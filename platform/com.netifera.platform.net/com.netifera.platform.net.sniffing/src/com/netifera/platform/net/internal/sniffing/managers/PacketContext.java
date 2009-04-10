package com.netifera.platform.net.internal.sniffing.managers;

import com.netifera.platform.net.pcap.ICaptureHeader;
import com.netifera.platform.net.sniffing.IPacketContext;

public class PacketContext implements IPacketContext {
	
	private final ICaptureHeader header;
	private Object tag;
	private boolean isAborted;
	
	public PacketContext(ICaptureHeader header, Object tag) {
		this.header = header;
		this.tag = tag;
	}
	
	public PacketContext(ICaptureHeader header) {
		this.header = header;
	}
	
	public ICaptureHeader getCaptureHeader() {
		return header;
	}
	
	public Object getPacketTag() {
		return tag;
	}
	
	public void setPacketTag(Object tag) {
		this.tag = tag;	
	}

	public void abortProcessing() {
		isAborted = true;		
	}

	public boolean isAborted() {
		return isAborted;
	}

}
