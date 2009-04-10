package com.netifera.platform.net.wifi.internal.sniffing.daemon.probe;

import java.io.Serializable;

import com.netifera.platform.net.daemon.sniffing.ISniffingModule;
import com.netifera.platform.net.sniffing.IPacketFilter;

public class ModuleRecord implements Serializable, ISniffingModule {

	private static final long serialVersionUID = -3183211928864072806L;
	private final String name;
	private final boolean enabled;
	
	public ModuleRecord(String name, boolean enabled) {
		this.name = name;
		this.enabled = enabled;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public IPacketFilter getFilter() {
		return null;
	}
}
