package com.netifera.platform.net.internal.daemon.sniffing;

import java.util.HashMap;
import java.util.Map;

import com.netifera.platform.api.dispatcher.DispatchException;
import com.netifera.platform.api.dispatcher.DispatchMismatchException;
import com.netifera.platform.api.dispatcher.IClientDispatcher;
import com.netifera.platform.api.dispatcher.IMessageDispatcher;
import com.netifera.platform.api.dispatcher.IMessageDispatcherService;
import com.netifera.platform.api.dispatcher.IMessageHandler;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.net.daemon.sniffing.ISniffingDaemon;
import com.netifera.platform.net.daemon.sniffing.ISniffingDaemonFactory;
import com.netifera.platform.net.internal.daemon.probe.CaptureFileProgress;
import com.netifera.platform.net.internal.daemon.probe.RemoteSniffingDaemon;
import com.netifera.platform.net.internal.daemon.probe.SniffingModuleOutput;

public class SniffingDaemonFactory implements ISniffingDaemonFactory {

	private ILogger logger;
	private Map<IProbe, RemoteSniffingDaemon> probeMap = new HashMap<IProbe, RemoteSniffingDaemon>();
	private IClientDispatcher clientDispatcher;


	public ISniffingDaemon createForProbe(IProbe probe, IEventHandler changeHandler) {
		if(probeMap.containsKey(probe)) {
			return probeMap.get(probe);
		}
		RemoteSniffingDaemon rsd = new RemoteSniffingDaemon(probe, logger, changeHandler);
		probeMap.put(probe, rsd);
		return rsd;
		
	}
	
	public ISniffingDaemon lookupForProbe(IProbe probe) {
		return probeMap.get(probe);
	}
	
	private void captureFileProgress(IMessenger messenger, CaptureFileProgress msg) {
		final RemoteSniffingDaemon rsd = probeMap.get(messenger.getProbe());
		if(rsd != null) {
			rsd.captureFileProgress(msg);
		}
	}
	
	private void sniffingModuleOutput(IMessenger messenger, SniffingModuleOutput msg) {
		final RemoteSniffingDaemon rsd = probeMap.get(messenger.getProbe());
		if(rsd != null) {
			rsd.sniffingModuleOutput(msg.getMessage());
		}
	}
	
	private void registerHandlers(IMessageDispatcher dispatcher) {
		IMessageHandler handler = new IMessageHandler() {

			public void call(IMessenger messenger, IProbeMessage message)
					throws DispatchException {
				if (message instanceof CaptureFileProgress) {
					captureFileProgress(messenger, (CaptureFileProgress) message);
				} else if(message instanceof SniffingModuleOutput) {
					sniffingModuleOutput(messenger, (SniffingModuleOutput) message);
				} else {
					throw new DispatchMismatchException(message);
				}				
				
			}
		};
		dispatcher.registerMessageHandler(CaptureFileProgress.ID, handler);
		dispatcher.registerMessageHandler(SniffingModuleOutput.ID, handler);

	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Sniffing Daemon");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		logger = null;
	}
	
	protected void setDispatcher(IMessageDispatcherService dispatcher) {
		clientDispatcher = dispatcher.getClientDispatcher();
		registerHandlers(clientDispatcher);
	}
	
	protected void unsetDispatcher(IMessageDispatcherService dispatcher) {
		clientDispatcher = null;
	}

}
