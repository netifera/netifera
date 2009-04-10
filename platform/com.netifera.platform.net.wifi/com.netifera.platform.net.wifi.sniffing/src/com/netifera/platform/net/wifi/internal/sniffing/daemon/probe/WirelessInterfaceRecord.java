package com.netifera.platform.net.wifi.internal.sniffing.daemon.probe;

import java.io.Serializable;

import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.pcap.IPacketCapture;
import com.netifera.platform.net.pcap.IPacketHandler;
import com.netifera.platform.net.wifi.pcap.IWirelessCaptureInterface;

public class WirelessInterfaceRecord implements Serializable, IWirelessCaptureInterface {

	
	private static final long serialVersionUID = -3197786620423339097L;
	private final String name;
	private final String label;
	private final boolean available;
	private final boolean enabled;
	private final boolean hasMonitorMode;
	
	public WirelessInterfaceRecord(String name, boolean available, boolean enable) {
		this(name, null, available, enable, true);
	}
	public WirelessInterfaceRecord(String name, String label, boolean available, boolean enabled, boolean monitor) {
		this.name = name;
		this.label = label;
		this.available = available;
		this.enabled = enabled;
		this.hasMonitorMode = monitor;
	}
	public boolean captureAvailable() {
		return available;
	}

	public String getName() {
		return name;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public String toString() {
		return label;
	}
	public ICaptureInterface getInterface() {
		return this;
	}
	public boolean isMonitorModeCapable() {
		return hasMonitorMode;
	}
	public IPacketCapture pcapCreate(int snaplen, boolean promiscuous,
			int timeout, IPacketHandler handler) {
		// TODO Auto-generated method stub
		return null;
	}

}
