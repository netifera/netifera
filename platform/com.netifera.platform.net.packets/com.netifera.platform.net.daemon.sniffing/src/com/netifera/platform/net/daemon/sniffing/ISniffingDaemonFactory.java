package com.netifera.platform.net.daemon.sniffing;

import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.probe.IProbe;

public interface ISniffingDaemonFactory {
	ISniffingDaemon createForProbe(IProbe probe, IEventHandler changeHandler);
	ISniffingDaemon lookupForProbe(IProbe probe);
}
