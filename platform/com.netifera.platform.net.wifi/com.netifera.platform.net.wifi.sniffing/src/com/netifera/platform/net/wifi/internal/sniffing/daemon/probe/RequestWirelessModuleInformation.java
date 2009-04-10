package com.netifera.platform.net.wifi.internal.sniffing.daemon.probe;

import java.util.List;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class RequestWirelessModuleInformation extends ProbeMessage {
	
	private static final long serialVersionUID = -2777763201274077893L;
	public final static String ID = "RequestWirelessModuleInformation";
	private final List<ModuleRecord> modules;

	public RequestWirelessModuleInformation() {
		super(ID);
		modules = null;
	}
	
	public RequestWirelessModuleInformation createResponse(List<ModuleRecord> modules) {
		return new RequestWirelessModuleInformation(modules, getSequenceNumber());
	}
	
	private RequestWirelessModuleInformation(List<ModuleRecord> modules, int sequenceNumber) {
		super(ID);
		this.modules = modules;
		setSequenceNumber(sequenceNumber);
		markAsResponse();
	}
	
	public List<ModuleRecord> getModuleRecords() {
		return modules;
	}
}
