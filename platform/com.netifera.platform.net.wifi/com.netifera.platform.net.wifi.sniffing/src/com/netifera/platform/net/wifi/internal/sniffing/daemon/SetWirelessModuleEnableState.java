package com.netifera.platform.net.wifi.internal.sniffing.daemon;

import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.api.dispatcher.ProbeMessage;
import com.netifera.platform.net.wifi.internal.sniffing.daemon.probe.ModuleRecord;

public class SetWirelessModuleEnableState extends ProbeMessage {
	
	private static final long serialVersionUID = 1772126450931080489L;
	public final static String ID = "SetWirelessModuleEnableState";
	private final List<ModuleRecord> modules;
	public SetWirelessModuleEnableState(List<ModuleRecord> modules) {
		super(ID);
		this.modules = modules;
	}
	
	public SetWirelessModuleEnableState(ModuleRecord module) {
		super(ID);
		this.modules = new ArrayList<ModuleRecord>();
		this.modules.add(module);
	}
	
	public List<ModuleRecord> getModuleRecords() {
		return modules;
	}

}
