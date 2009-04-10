package com.netifera.platform.net.wifi.internal.sniffing.daemon;

import java.util.HashMap;
import java.util.Map;

import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.net.daemon.sniffing.ISniffingDaemon;
import com.netifera.platform.net.daemon.sniffing.ISniffingDaemonFactory;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffingDaemon;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffingDaemonFactory;

public class WifiSniffingDaemonFactory implements IWifiSniffingDaemonFactory {

	private ISniffingDaemonFactory sniffingDaemonFactory;
	private ILogger logger;
	
	private Map<IProbe, RemoteWifiSniffingDaemon> probeMap = 
		new HashMap<IProbe, RemoteWifiSniffingDaemon>();
	
	public IWifiSniffingDaemon createForProbe(IProbe probe, IEventHandler changeHandler) {
		if(probeMap.containsKey(probe)) {
			return probeMap.get(probe);
		}
		final ISniffingDaemon sniffingDaemonStub = sniffingDaemonFactory.createForProbe(probe, changeHandler);
		final RemoteWifiSniffingDaemon remote = new RemoteWifiSniffingDaemon(probe, logger, sniffingDaemonStub);
		probeMap.put(probe, remote);
		return remote;
	}
	
	protected void setSniffingDaemonFactory(ISniffingDaemonFactory factory) {
		sniffingDaemonFactory = factory;
	}
	
	protected void unsetSniffingDaemonFactory(ISniffingDaemonFactory factory) {
		sniffingDaemonFactory = null;
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Wifi Sniffing Daemon");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}

}
