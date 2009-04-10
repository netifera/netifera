package com.netifera.platform.net.internal.daemon.probe;

import java.util.List;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class RequestInterfaceInformation extends ProbeMessage {
	
	private static final long serialVersionUID = 6258301182787202475L;

	public final static String ID = "RequestInterfaceInformation";

	private final List<InterfaceRecord> interfaces;
	
	public RequestInterfaceInformation() {
		super(ID);
		interfaces = null;
	}
	
	public RequestInterfaceInformation createResponse(List<InterfaceRecord> interfaces) {
		return new RequestInterfaceInformation(interfaces, getSequenceNumber());
	}
	
	private RequestInterfaceInformation(List<InterfaceRecord> interfaces, int sequenceNumber) {
		super(ID);
		this.interfaces = interfaces;
		setSequenceNumber(sequenceNumber);
		markAsResponse();
	}
	
	public List<InterfaceRecord> getInterfaceRecords() {
		return interfaces;
	}

}
