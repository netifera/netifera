	package com.netifera.platform.net.wifi.sniffing;

import java.util.Collection;
import java.util.Set;

import com.netifera.platform.net.daemon.sniffing.ISniffingDaemon;
import com.netifera.platform.net.daemon.sniffing.ISniffingModule;
import com.netifera.platform.net.wifi.pcap.IWirelessCaptureInterface;

public interface IWifiSniffingDaemon extends ISniffingDaemon {
	
	Collection<IWirelessCaptureInterface> getWirelessInterfaces();
	void setWirelessEnabled(IWirelessCaptureInterface iface, boolean enable);
	void setWirelessEnabled(ISniffingModule module, boolean enable);
	Set<ISniffingModule> getWirelessModules();


}
