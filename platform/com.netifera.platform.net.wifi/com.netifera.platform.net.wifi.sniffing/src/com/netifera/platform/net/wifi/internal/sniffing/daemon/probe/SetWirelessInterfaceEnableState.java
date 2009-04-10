package com.netifera.platform.net.wifi.internal.sniffing.daemon.probe;

import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class SetWirelessInterfaceEnableState extends ProbeMessage {
	
	private static final long serialVersionUID = 1248467369840123720L;

	
	public final static String ID = "SetWirelessInterfaceEnableState";
	
	private final List<WirelessInterfaceRecord> interfaces;
	
	public SetWirelessInterfaceEnableState(List<WirelessInterfaceRecord> interfaces) {
		super(ID);
		this.interfaces = interfaces;
	}
	
	public SetWirelessInterfaceEnableState(WirelessInterfaceRecord iface) {
		super(ID);
		this.interfaces = new ArrayList<WirelessInterfaceRecord>();
		this.interfaces.add(iface);
	}
	
	public List<WirelessInterfaceRecord> getInterfaceRecords() {
		return interfaces;
	}

}
