package com.netifera.platform.net.internal.daemon.probe;

import java.io.Serializable;

import com.netifera.platform.net.pcap.ICaptureInterface;

public class InterfaceRecord implements Serializable, ICaptureInterface {
	
	private static final long serialVersionUID = 8395617208863728394L;
	private final String name;
	private final String label;
	private final boolean available;
	private final boolean enabled;
	
	public InterfaceRecord(String name, boolean available, boolean enabled) {
		this.name = name;
		this.label = null;
		this.available = available;
		this.enabled = enabled;
	}
	
	public InterfaceRecord(String name, String label, boolean available, boolean enabled) {
		this.name = name;
		this.label = label;
		this.available = available;
		this.enabled = enabled;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean captureAvailable() {
		return isAvailable();
	}
	
	public boolean isAvailable() {
		return available;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public String toString() {
		return label;
	}

}
