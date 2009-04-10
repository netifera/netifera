package com.netifera.platform.host.processes;

import com.netifera.platform.api.probe.IProbe;

public interface IProcessManagerFactory {
	IProcessManager createForProbe(IProbe probe);
}
