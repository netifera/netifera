package com.netifera.platform.net.wifi.sniffing;

import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.probe.IProbe;

public interface IWifiSniffingDaemonFactory  {
	IWifiSniffingDaemon createForProbe(IProbe probe, IEventHandler changeHandler);
}
