package com.netifera.platform.host.terminal;

import com.netifera.platform.api.probe.IProbe;

public interface ITerminalManagerFactory {
	
	public ITerminalManager createForProbe(IProbe probe);
	
}
