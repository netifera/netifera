package com.netifera.platform.net.wifi.internal.sniffing.daemon;

import com.netifera.platform.net.daemon.sniffing.IPacketModuleContext;
import com.netifera.platform.net.pcap.ICaptureHeader;
import com.netifera.platform.net.sniffing.IPacketContext;

public class PacketModuleContext implements IPacketModuleContext {
	
	private final IPacketContext packetContext;
	private final long spaceId;
	
	PacketModuleContext(IPacketContext ctx, long spaceId) {
		this.packetContext = ctx;
		this.spaceId = spaceId;
	}

	public ICaptureHeader getCaptureHeader() {
		return packetContext.getCaptureHeader();
	}

	public long getRealm() {
		Object o = packetContext.getPacketTag();
		if(o == null || !(o instanceof Long))
			return -1;
		return ((Long)o).longValue();
	}

	public long getSpaceId() {
		return spaceId;
	}

	public void setRealm(long realm) {
		packetContext.setPacketTag(new Long(realm));
	}
	
	public void printOutput(String message) {
		throw new RuntimeException("Not implemented yet");
	}

	public void abortProcessing() {
		packetContext.abortProcessing();		
	}


}
