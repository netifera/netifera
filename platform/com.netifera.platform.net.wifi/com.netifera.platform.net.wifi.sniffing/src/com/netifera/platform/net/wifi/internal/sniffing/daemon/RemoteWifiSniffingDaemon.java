package com.netifera.platform.net.wifi.internal.sniffing.daemon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.dispatcher.StatusMessage;
import com.netifera.platform.net.daemon.sniffing.ISniffingDaemon;
import com.netifera.platform.net.daemon.sniffing.ISniffingModule;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.ICaptureFileInterface;
import com.netifera.platform.net.sniffing.util.ICaptureFileProgress;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.ModuleRecord;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.RequestWirelessInterfaceInformation;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.RequestWirelessModuleInformation;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.SetWirelessInterfaceEnableState;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.StartWifiSniffer;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.StopWifiSniffer;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.WirelessInterfaceRecord;
import com.netifera.platform.net.wifi.pcap.IWirelessCaptureInterface;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffingDaemon;

public class RemoteWifiSniffingDaemon implements IWifiSniffingDaemon {

	private final IProbe probe;
	private final ILogger logger;
	private String messengerError;

	
	/* stub for remote sniffing daemon */
	private final ISniffingDaemon sniffingDaemon;
	
	RemoteWifiSniffingDaemon(IProbe probe, ILogger logger, ISniffingDaemon sniffingDaemon) {
		this.probe = probe;
		this.logger = logger;
		this.sniffingDaemon = sniffingDaemon;
	}
	
	public Collection<IWirelessCaptureInterface> getWirelessInterfaces() {
		final List<WirelessInterfaceRecord> interfaceRecords = getInterfaceRecords();
		if(interfaceRecords == null) 
			return Collections.emptyList();
		return new ArrayList<IWirelessCaptureInterface>(interfaceRecords);
		
	}
	
	private List<WirelessInterfaceRecord> getInterfaceRecords() {
		final RequestWirelessInterfaceInformation response = 
			(RequestWirelessInterfaceInformation) exchangeMessage(new RequestWirelessInterfaceInformation());
		if(response == null) {
			logger.warning("Failed to get wireless interface information: " + getLastError());
			return Collections.emptyList();
		}
	
		return response.getInterfaceRecords();
	}
	
	public Set<ISniffingModule> getWirelessModules() {
		final List<ModuleRecord> moduleRecords = getModuleRecords();
		if(moduleRecords == null)
			return Collections.emptySet();
		
		return new HashSet<ISniffingModule>(moduleRecords);
	}
	
	private List<ModuleRecord> getModuleRecords() {
		final RequestWirelessModuleInformation response = 
			(RequestWirelessModuleInformation) exchangeMessage(new RequestWirelessModuleInformation());
		if(response == null) {
			logger.warning("Failed to get wireless module information: " + getLastError());
			return null;
		}
		return response.getModuleRecords();
	}
	
	private IProbeMessage exchangeMessage(IProbeMessage message) {
		try {
			final IProbeMessage response = probe.getMessenger().exchangeMessage(message);
			if(response instanceof StatusMessage) {
				return null;
			} else {
				return response;
			}
		} catch(MessengerException e) {
			messengerError = e.getMessage();
			return null;
			
		}
	}
	
	private boolean sendMessage(IProbeMessage message) {
		try {
			probe.getMessenger().sendMessage(message);
			return true;
		} catch (MessengerException e) {
			messengerError = e.getMessage();
			return false;
		}
	}
	
	private String getLastError() {
		return messengerError;
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

	public boolean isEnabled(ICaptureInterface iface) {
		return sniffingDaemon.isEnabled(iface);
	}

	public boolean isEnabled(ISniffingModule module) {
		if(module instanceof ModuleRecord) {
			return ((ModuleRecord)module).isEnabled();
		}
		return sniffingDaemon.isEnabled(module);
	}

	public void runCaptureFile(long spaceId, ICaptureFileInterface iface,
			ICaptureFileProgress progress) {
		sniffingDaemon.runCaptureFile(spaceId, iface, progress);		
	}

	public void setEnabled(ICaptureInterface iface, boolean enable) {
		sniffingDaemon.setEnabled(iface, enable);		
	}

	public void setWirelessEnabled(IWirelessCaptureInterface iface, boolean enable) {
		final WirelessInterfaceRecord interfaceRecord = new WirelessInterfaceRecord(iface.getName(), iface.captureAvailable(), enable);
		if(!sendMessage(new SetWirelessInterfaceEnableState(interfaceRecord))) {
			logger.warning("Failed to enable wireless interface: " + getLastError());
		}
	}
	
	public void setWirelessEnabled(ISniffingModule module, boolean enable) {
		final ModuleRecord moduleRecord = new ModuleRecord(module.getName(), enable);
		if(!sendMessage(new SetWirelessModuleEnableState(moduleRecord))) {
			logger.warning("Failed to enable wireless sniffing module " + getLastError());
		}
	}
	public void setEnabled(ISniffingModule module, boolean enable) {
		sniffingDaemon.setEnabled(module, enable);		
	}

	public boolean isRunning() {
		return sniffingDaemon.isRunning();
	}

	public void start(long spaceId) {
		if(!sendMessage(new StartWifiSniffer(spaceId))) {
			logger.warning("Failed to start wifi sniffing daemon " + getLastError());
		}
	}

	public void stop() {
		if(!sendMessage(new StopWifiSniffer())) {
			logger.warning("Failed to stop wifi sniffing daemon " + getLastError());
		}
	}

}
