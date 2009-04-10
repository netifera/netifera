package com.netifera.platform.net.internal.daemon.probe;

import java.util.List;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class RequestModuleInformation extends ProbeMessage {
	
	private static final long serialVersionUID = 2551440464627254907L;

	public final static String ID = "RequestModuleInformation";

	private final List<ModuleRecord> modules;
	
	public RequestModuleInformation() {
		super(ID);
		modules = null;
	}
	
	public RequestModuleInformation createResponse(List<ModuleRecord> modules) {
		return new RequestModuleInformation(modules, getSequenceNumber());
	}
	
	private RequestModuleInformation(List<ModuleRecord> modules, int sequenceNumber) {
		super(ID);
		this.modules = modules;
		setSequenceNumber(sequenceNumber);
		markAsResponse();
	}
	
	public List<ModuleRecord> getModuleRecords() {
		return modules;
	}
}

