package com.netifera.platform.host.processes.probe;

import java.util.HashMap;
import java.util.Map;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.host.processes.IProcessManager;
import com.netifera.platform.host.processes.IProcessManagerFactory;

public class ProcessManagerFactory implements IProcessManagerFactory {

	private ILogger logger;
	private Map<IProbe, RemoteProcessManager> probeMap =
		new HashMap<IProbe, RemoteProcessManager>();
	
	public IProcessManager createForProbe(IProbe probe) {
		if(probeMap.containsKey(probe))
			return probeMap.get(probe);
		final RemoteProcessManager rpm = new RemoteProcessManager(probe, logger);
		probeMap.put(probe, rpm);
		return rpm;
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Process Manager");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}
}
