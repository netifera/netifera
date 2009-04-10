package com.netifera.platform.net.wifi.internal.sniffing.daemon.probe;

import java.util.List;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class RequestWirelessInterfaceInformation extends ProbeMessage {
	
	private static final long serialVersionUID = 139495148790803491L;
	public final static String ID = "RequestWirelessInterfaceInformation";
	private final List<WirelessInterfaceRecord> interfaces;
	
	public RequestWirelessInterfaceInformation() {
		super(ID);
		interfaces = null;
	}
	
	public RequestWirelessInterfaceInformation createResponse(List<WirelessInterfaceRecord> interfaces) {
		return new RequestWirelessInterfaceInformation(interfaces, getSequenceNumber());
	}
	
	private RequestWirelessInterfaceInformation(List<WirelessInterfaceRecord> interfaces, int sequenceNumber) {
		super(ID);
		this.interfaces = interfaces;
		setSequenceNumber(sequenceNumber);
		markAsResponse();
	}
	public List<WirelessInterfaceRecord> getInterfaceRecords() {
		return interfaces;
	}
}
