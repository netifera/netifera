package com.netifera.platform.net.internal.sniffing.managers;

import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.stream.IBlockSniffer;
import com.netifera.platform.net.sniffing.stream.IBlockSnifferHandle;

public class TCPBlockSnifferHandle implements IBlockSnifferHandle {
	
	private final static int DEFAULT_LIMIT = 1024;
	private int clientLimit;
	private int serverLimit;
	private int totalLimit = DEFAULT_LIMIT;
	
	private Object defaultTag;
	
	private final IBlockSniffer sniffer;
	private final TCPManager manager;

	public TCPBlockSnifferHandle(TCPManager tcpManager,
			IPacketFilter filter, IBlockSniffer sniffer) {
		this.sniffer = sniffer;
		this.manager = tcpManager;
	}
	
	public void setClientLimit(int byteCount) {
		resetLimits();
		clientLimit = byteCount;
	}
	
	public int getClientLimit() {
		return clientLimit;
	}
	
	public void setServerLimit(int byteCount) {
		resetLimits();
		serverLimit = byteCount;
	}
	
	public int getServerLimit() {
		return serverLimit;
	}
	
	public void setTotalLimit(int byteCount) {
		resetLimits();
		totalLimit = byteCount;
	}

	public int getTotalLimit() {
		return totalLimit;
	}
	
	private void resetLimits() {
		clientLimit = 0;
		serverLimit = 0;
		totalLimit = 0;
	}

	public IBlockSniffer getSniffer() {
		return sniffer;
	}
	public ICaptureInterface getInterface() {
		return manager.getInterface();
	}

	public void register() {
		manager.registerBlockHandle(this);
	}

	public void unregister() {
		manager.unregisterBlockHandle(this);
		
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
