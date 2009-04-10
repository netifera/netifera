package com.netifera.platform.net.internal.sniffing.managers;

import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.IPacketSnifferHandle;
import com.netifera.platform.net.sniffing.util.ISniffingEngineEx;

public interface IPacketManager<T extends IPacketHeader> {
	void registerSniffer(IPacketSnifferHandle<T> handle);
	void registerPrioritySniffer(IPacketSnifferHandle<T> handle);
	void unregisterSniffer(IPacketSnifferHandle<T> handle);
	ICaptureInterface getInterface();
	ISniffingEngineEx getSniffingEngine();
}
