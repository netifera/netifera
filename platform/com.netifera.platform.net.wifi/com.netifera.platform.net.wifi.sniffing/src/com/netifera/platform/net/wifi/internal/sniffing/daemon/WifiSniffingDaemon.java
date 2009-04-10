package com.netifera.platform.net.wifi.internal.sniffing.daemon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.ComponentContext;

import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.net.daemon.sniffing.ISniffingModule;
import com.netifera.platform.net.daemon.sniffing.model.NetworkInterfaceEntity;
import com.netifera.platform.net.daemon.sniffing.model.SniffingSessionEntity;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.ModuleRecord;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.RequestWirelessInterfaceInformation;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.RequestWirelessModuleInformation;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.SetWirelessInterfaceEnableState;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.StartWifiSniffer;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.StopWifiSniffer;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.WirelessInterfaceRecord;
import com.netifera.platform.net.wifi.pcap.IWirelessCaptureInterface;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffer;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffingDaemon;

public class WifiSniffingDaemon extends AbstractWifiSniffingDaemon implements IWifiSniffingDaemon {

	private final Set<IWifiSniffer> modules;
	private final Set<IWirelessCaptureInterface> enabledInterfaces;	
	private final Set<EnabledWifiModule> enabledModules;
	private final Map<String, ISniffingModule> moduleByName = new HashMap<String, ISniffingModule>();

	private boolean running;
	private boolean isActivated;

	
	public WifiSniffingDaemon() {
		modules = new HashSet<IWifiSniffer>();
		enabledInterfaces = new HashSet<IWirelessCaptureInterface>();
		enabledModules = new HashSet<EnabledWifiModule>();
	}
	
	public Collection<IWirelessCaptureInterface> getWirelessInterfaces() {
		return wirelessSniffingEngine.getWifiInterfaces();
	}

	public Set<ISniffingModule> getWirelessModules() {
		return new HashSet<ISniffingModule>(modules);
	}

	public boolean isEnabled(ICaptureInterface iface) {
		for(EnabledWifiModule m : enabledModules) {
			if(m.getModule().equals(m))
				return true;
		}
		return sniffingDaemon.isEnabled(iface);
	}

	
	public void disableAllInterfaces() {
		enabledInterfaces.clear();
		sniffingDaemon.disableAllInterfaces();
	}

	public void setWirelessEnabled(ISniffingModule module, boolean enable) {
		if(running) {
			throw new IllegalStateException("Cannot configure modules while daemon is running");
		}
		if(!getWirelessModules().contains(module)) {
			throw new IllegalArgumentException("Unknown wireless module passed to setWirelessEnabled()");
		}
		if(enable) {
			if(findEnabledModule(module) == null) {
				enabledModules.add(new EnabledWifiModule(wirelessSniffingEngine, (IWifiSniffer) module, logger));
				
			}
		} else {
			EnabledWifiModule ewm = findEnabledModule(module);
			if(ewm != null) 
				enabledModules.remove(ewm);
		}
	}
	
	private EnabledWifiModule findEnabledModule(ISniffingModule module) {
		for(EnabledWifiModule m : enabledModules) {
			if(m.getModule().equals(module))
				return m;
		}
		return null;
	}
	public void setEnabled(ISniffingModule module, boolean enable) {
		sniffingDaemon.setEnabled(module, enable);		
	}

	public boolean isRunning() {
		return running;
	}

	public void start(long spaceId) {
		if(running)
			return;
		
		final long realm = probeManager.getLocalProbe().getEntity().getId();
		final SniffingSessionEntity session = sniffingEntityFactory.createSniffingSession(realm, spaceId);
		start(spaceId, session.getId());
				
	}

	public void start(long spaceId, long realm) {
		if(running)
			return;
		
		final Set<WifiDaemonInterface> interfaces = new HashSet<WifiDaemonInterface>();
		sniffingDaemon.disableAllInterfaces();
		
		for(IWirelessCaptureInterface iface : enabledInterfaces) {
			
			NetworkInterfaceEntity interfaceEntity = sniffingEntityFactory.createNetworkInterface(realm, spaceId, iface.getName());
			interfaces.add(new WifiDaemonInterface(iface, interfaceEntity.getId()));
			sniffingDaemon.setEnabled(iface.getInterface(), true);
		}
		
		for(EnabledWifiModule module : enabledModules) {
			module.start(interfaces, spaceId);	
		}
		final Set<ICaptureInterface> ifs = new HashSet<ICaptureInterface>(enabledInterfaces);
		sniffingDaemon.start(ifs, wirelessSniffingEngine, spaceId, realm);
		running = true;
	}
	
	public void stop() {
		if(!running)
			return;
		sniffingDaemon.stop();	
		for(EnabledWifiModule module : enabledModules) {
			module.stop();
		}
		running = false;
	}
	
	public void setWirelessEnabled(IWirelessCaptureInterface iface, boolean enable) {
		setWirelessInterfaceEnabled(iface, enable);
	}
	
	private void setWirelessInterfaceEnabled(IWirelessCaptureInterface iface, boolean enable) {
		if(running)
			throw new IllegalStateException("Cannot change wireless interface configuration while daemon is running");
		if(!getWirelessInterfaces().contains(iface)) 
			throw new IllegalArgumentException("Unknown interface passed to setWirelessInterfaceEnabled()");
		if(enable && !iface.captureAvailable())
			throw new IllegalArgumentException("Cannot enable unavailable interface");
	
		if(enable)
			enabledInterfaces.add(iface);
		else
			enabledInterfaces.remove(iface);
	}
	
	
	protected void requestInterfaceInformation(IMessenger messenger, RequestWirelessInterfaceInformation message) throws MessengerException {
		final ArrayList<WirelessInterfaceRecord> results = new ArrayList<WirelessInterfaceRecord>();
		for(IWirelessCaptureInterface iface : getWirelessInterfaces()) {
			results.add(new WirelessInterfaceRecord(iface.getName(), iface.toString(), iface.captureAvailable(), true, iface.isMonitorModeCapable()));
		}
		messenger.emitMessage(message.createResponse(results));
	
	}
	
	protected void requestModuleInformation(IMessenger messenger, RequestWirelessModuleInformation message) throws MessengerException {
		final List<ModuleRecord> modules = new ArrayList<ModuleRecord>();
		for(ISniffingModule module : getWirelessModules()) {
			modules.add(new ModuleRecord(module.getName(), isWirelessModuleEnabled(module)));
		}
		messenger.emitMessage(message.createResponse(modules));
	}
	
	private boolean isWirelessModuleEnabled(ISniffingModule module) {
		return findEnabledModule(module) != null;
	}
	
	protected void startWifiSniffer(IMessenger messenger, StartWifiSniffer msg) throws MessengerException {
		start(msg.getSpaceId());
		messenger.respondOk(msg);
	}
	
	protected void stopWifiSniffer(IMessenger messenger, StopWifiSniffer msg) throws MessengerException {
		stop();
		messenger.respondOk(msg);
	}
	
	protected void setWirelessInterfaceEnableState(IMessenger messenger, SetWirelessInterfaceEnableState msg) throws MessengerException {
		for(WirelessInterfaceRecord record : msg.getInterfaceRecords()) {
			IWirelessCaptureInterface wifiInterface = lookupInterfaceByName(record.getName());
			if(wifiInterface == null) {
				logger.warning("No wireless interface found with name " + record.getName());
			} else {
				setWirelessInterfaceEnabled(wifiInterface, record.isEnabled());
			}
		}
		messenger.respondOk(msg);
	}
	
	
	protected void setWirelessModuleEnableState(IMessenger messenger, SetWirelessModuleEnableState msg) throws MessengerException {
		for(ModuleRecord module : msg.getModuleRecords()) {
			final ISniffingModule sniffingModule = moduleByName.get(module.getName());
			if(sniffingModule == null) {
				logger.warning("No wireless sniffing module found with name " + module.getName());
			} else {
				setWirelessEnabled(sniffingModule, module.isEnabled());
			}
		}
		messenger.respondOk(msg);
	}
	
	private IWirelessCaptureInterface lookupInterfaceByName(String name) {
		for(IWirelessCaptureInterface iface : getWirelessInterfaces()) {
			if(iface.getName().equals(name))
				return iface;
		}
		return null;
	}
	
	private void enableAllInterfaces() {
		for(IWirelessCaptureInterface iface : getWirelessInterfaces())
			setWirelessEnabled(iface, true);
	}
	
	private void enableAllModules() {
		for(ISniffingModule module : getWirelessModules()) 
			setWirelessEnabled(module, true);
		
	}
	protected void activate(ComponentContext ctx) {
		synchronized(modules) {
			isActivated = true;
			enableAllInterfaces();
			enableAllModules();
		}
	
	}
	
	protected void deactivate(ComponentContext ctx) {
		
	}
	
	protected void registerModule(IWifiSniffer module) {
		synchronized(modules) {
			modules.add(module);
			moduleByName.put(module.getName(), module);
			if(isActivated)
				setEnabled(module, true);
		}
		
	}
	
	protected void unregisterModule(IWifiSniffer module) {
		synchronized(modules) {
			modules.remove(module);
			moduleByName.remove(module.getName());
		}
	}
	
}