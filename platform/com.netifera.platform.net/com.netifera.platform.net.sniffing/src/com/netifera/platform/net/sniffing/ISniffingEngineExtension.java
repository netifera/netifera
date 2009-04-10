package com.netifera.platform.net.sniffing;

import com.netifera.platform.net.pcap.IPacketCaptureFactoryService;

public interface ISniffingEngineExtension {
	IPacketCaptureFactoryService getPacketCaptureExtension();
	

}
