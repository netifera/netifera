package com.netifera.platform.host.terminal;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.system.ISystemService;
import com.netifera.platform.host.terminal.linux.PseudoTerminal;

public class PseudoTerminalFactory implements IPseudoTerminalFactory {

	private ILogger logger;
	private ISystemService systemService;
	
	public ITerminal openTerminal(String command, ITerminalOutputHandler outputHandler) {
		PseudoTerminal pty = new PseudoTerminal(command, outputHandler, logger, systemService);
		if(pty.open())
			return pty;
		return null;
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Terminal");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}
	
	protected void setSystemService(ISystemService system) {
		systemService = system;
	}
	
	protected void unsetSystemService(ISystemService system) {
		
	}

}
