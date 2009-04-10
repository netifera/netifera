package com.netifera.platform.host.terminal.probe;

import java.util.HashMap;
import java.util.Map;

import com.netifera.platform.api.dispatcher.DispatchException;
import com.netifera.platform.api.dispatcher.DispatchMismatchException;
import com.netifera.platform.api.dispatcher.IMessageDispatcher;
import com.netifera.platform.api.dispatcher.IMessageDispatcherService;
import com.netifera.platform.api.dispatcher.IMessageHandler;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.host.terminal.ITerminalManager;
import com.netifera.platform.host.terminal.ITerminalManagerFactory;

public class TerminalManagerFactory implements ITerminalManagerFactory {

	private ILogger logger;
	private Map<IProbe, RemoteTerminalManager> probeMap =
		new HashMap<IProbe, RemoteTerminalManager>();
	public ITerminalManager createForProbe(IProbe probe) {
		if(probeMap.containsKey(probe))
			return probeMap.get(probe);
		
		final RemoteTerminalManager rtm = new RemoteTerminalManager(probe, logger);
		probeMap.put(probe, rtm);
		return rtm;
	}
	
	
	private void registerHandlers(IMessageDispatcher dispatcher) {
		IMessageHandler handler = new IMessageHandler() {

			public void call(IMessenger messenger, IProbeMessage message)
					throws DispatchException {
				RemoteTerminalManager rtm = (RemoteTerminalManager) createForProbe(messenger.getProbe());
				if(message instanceof TerminalOutput) {
					rtm.terminalOutput((TerminalOutput) message);
				} else if(message instanceof TerminalClosed) {
					rtm.terminalClosed((TerminalClosed) message);
				} else {
					throw new DispatchMismatchException(message);
				}				
			}
		};
		dispatcher.registerMessageHandler(TerminalOutput.ID, handler);
		dispatcher.registerMessageHandler(TerminalClosed.ID, handler);
	}
	
	protected void setMessageDispatcher(IMessageDispatcherService dispatcher) {
		registerHandlers(dispatcher.getClientDispatcher());
	}
	
	protected void unsetMessageDispatcher(IMessageDispatcherService dispatcher) {
		
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Terminal Manager");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}

}
