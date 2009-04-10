package com.netifera.platform.net.internal.daemon.probe;

import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class SetInterfaceEnableState extends ProbeMessage {
	
	private static final long serialVersionUID = 2731915577658760233L;

	public final static String ID = "SetInterfaceEnableState";
	
	private final List<InterfaceRecord> interfaces;
	public SetInterfaceEnableState(List<InterfaceRecord> interfaces) {
		super(ID);
		this.interfaces = interfaces;
	}
	
	public SetInterfaceEnableState(InterfaceRecord iface) {
		super(ID);
		this.interfaces = new ArrayList<InterfaceRecord>();
		this.interfaces.add(iface);
	}

	public List<InterfaceRecord> getInterfaceRecords() {
		return interfaces;
	}

}
