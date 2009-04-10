package com.netifera.platform.net.daemon.sniffing;

import java.util.Set;

import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.ISniffingEngineService;

public interface ISniffingDaemonEx extends ISniffingDaemon {
	
	void start(long spaceId, long realm);
	void start(Set<ICaptureInterface> interfaces, ISniffingEngineService sniffingEngine, long spaceId, long realm);
	void disableAllInterfaces();


}
