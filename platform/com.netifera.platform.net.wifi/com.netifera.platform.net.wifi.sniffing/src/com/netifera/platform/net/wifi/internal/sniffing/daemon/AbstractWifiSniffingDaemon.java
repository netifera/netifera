package com.netifera.platform.net.wifi.internal.sniffing.daemon;

import java.util.Collection;
import java.util.Set;

import com.netifera.platform.api.dispatcher.DispatchException;
import com.netifera.platform.api.dispatcher.DispatchMismatchException;
import com.netifera.platform.api.dispatcher.IMessageDispatcher;
import com.netifera.platform.api.dispatcher.IMessageDispatcherService;
import com.netifera.platform.api.dispatcher.IMessageHandler;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.net.daemon.sniffing.ISniffingDaemonEx;
import com.netifera.platform.net.daemon.sniffing.ISniffingModule;
import com.netifera.platform.net.daemon.sniffing.model.ISniffingEntityFactory;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.ICaptureFileInterface;
import com.netifera.platform.net.sniffing.util.ICaptureFileProgress;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.RequestWirelessInterfaceInformation;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.RequestWirelessModuleInformation;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.SetWirelessInterfaceEnableState;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.StartWifiSniffer;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.StopWifiSniffer;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffingEngine;

abstract public class AbstractWifiSniffingDaemon  {
	
	protected ILogger logger;
	protected IWifiSniffingEngine wirelessSniffingEngine;
	protected ISniffingEntityFactory sniffingEntityFactory;
	protected IProbeManagerService probeManager;
	protected ISniffingDaemonEx sniffingDaemon;


	abstract protected void requestInterfaceInformation(IMessenger messenger, RequestWirelessInterfaceInformation message) throws MessengerException;
	abstract protected void startWifiSniffer(IMessenger messenger, StartWifiSniffer message) throws MessengerException;
	abstract protected void stopWifiSniffer(IMessenger messenger, StopWifiSniffer message) throws MessengerException;
	abstract protected void setWirelessInterfaceEnableState(IMessenger messenger, SetWirelessInterfaceEnableState message) throws MessengerException;
	abstract protected void requestModuleInformation(IMessenger messenger, RequestWirelessModuleInformation message) throws MessengerException;
	abstract protected void setWirelessModuleEnableState(IMessenger messenger, SetWirelessModuleEnableState message) throws MessengerException;
	
	
	private void registerHandlers(IMessageDispatcher dispatcher) {
		final IMessageHandler handler = new IMessageHandler() {
			
			public void call(IMessenger messenger, IProbeMessage message)
					throws DispatchException {
				try {
					dispatch(messenger, message);
				} catch(MessengerException e) {
					logger.warning("Error sending message response " + e.getMessage());
				}
			}

			
		};
		dispatcher.registerMessageHandler(RequestWirelessInterfaceInformation.ID, handler);
		dispatcher.registerMessageHandler(StartWifiSniffer.ID, handler);
		dispatcher.registerMessageHandler(StopWifiSniffer.ID, handler);
		dispatcher.registerMessageHandler(SetWirelessInterfaceEnableState.ID, handler);
		dispatcher.registerMessageHandler(RequestWirelessModuleInformation.ID, handler);
		dispatcher.registerMessageHandler(SetWirelessModuleEnableState.ID, handler);

	}
	
	private void dispatch(IMessenger messenger, IProbeMessage message) throws DispatchMismatchException, MessengerException {
		if(message instanceof RequestWirelessInterfaceInformation) {
			requestInterfaceInformation(messenger, (RequestWirelessInterfaceInformation) message);
		} else if(message instanceof StartWifiSniffer) {
			startWifiSniffer(messenger, (StartWifiSniffer) message);
		} else if(message instanceof StopWifiSniffer) {
			stopWifiSniffer(messenger, (StopWifiSniffer) message);
		} else if(message instanceof SetWirelessInterfaceEnableState) {
			setWirelessInterfaceEnableState(messenger, (SetWirelessInterfaceEnableState) message);
		} else if(message instanceof RequestWirelessModuleInformation) {
			requestModuleInformation(messenger, (RequestWirelessModuleInformation) message);
		} else if(message instanceof SetWirelessModuleEnableState) {
			setWirelessModuleEnableState(messenger, (SetWirelessModuleEnableState) message);
		}
		else {
			throw new DispatchMismatchException(message);
		}
	}
	protected void setDispatcher(IMessageDispatcherService dispatcher) {
		registerHandlers(dispatcher.getServerDispatcher());
	}
	
	protected void unsetDispatcher(IMessageDispatcherService dispatcher) {
		
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Wireless Probe Module");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}
	
	protected void setSniffingEntityFactory(ISniffingEntityFactory factory) {
		sniffingEntityFactory = factory;
	}
	
	protected void unsetSniffingEntityFactory(ISniffingEntityFactory factory) {
		
	}
	
	protected void setProbeManager(IProbeManagerService manager) {
		probeManager = manager;
	}
	
	protected void unsetProbeManager(IProbeManagerService manager) {
		
	}
	
	protected void setSniffingDaemon(ISniffingDaemonEx daemon) {
		sniffingDaemon = daemon;
	}
	
	protected void unsetSniffingDaemon(ISniffingDaemonEx daemon) {
		
	}
	
	protected void setSniffingEngine(IWifiSniffingEngine engine) {
		wirelessSniffingEngine = engine;
	}
	
	protected void unsetSniffingEngine(IWifiSniffingEngine engine) {
		
	}
	
	public void cancelCaptureFile() {
		sniffingDaemon.cancelCaptureFile();
		
	}

	public ICaptureFileInterface createCaptureFileInterface(String path) {
		return sniffingDaemon.createCaptureFileInterface(path);
	}

	public void enableInterfaces(Collection<ICaptureInterface> interfaces) {
		sniffingDaemon.enableInterfaces(interfaces);
	}

	public void enableModules(Set<ISniffingModule> enabledModuleSet) {
		sniffingDaemon.enableModules(enabledModuleSet);		
	}

	public Collection<ICaptureInterface> getInterfaces() {
		return sniffingDaemon.getInterfaces();
	}

	public Set<ISniffingModule> getModules() {
		return sniffingDaemon.getModules();
	}
	
	public boolean isEnabled(ISniffingModule module) {
		return sniffingDaemon.isEnabled(module);
	}

	public void runCaptureFile(long spaceId, ICaptureFileInterface iface,
			ICaptureFileProgress progress) {
		sniffingDaemon.runCaptureFile(spaceId, iface, progress);		
	}

	public void setEnabled(ICaptureInterface iface, boolean enable) {
		sniffingDaemon.setEnabled(iface, enable);		
	}
}


