package com.netifera.platform.net.daemon.sniffing;

import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.ISnifferHandle;

public interface ISniffingDaemonExtension {
	Class<? extends ISniffingModule> getModuleType();
	ISnifferHandle createHandle(ICaptureInterface iface, ISniffingModule sniffer);
}
