package com.netifera.platform.net.wifi.internal.sniffing.daemon;

import com.netifera.platform.net.wifi.pcap.IWirelessCaptureInterface;

public class WifiDaemonInterface {
	private final IWirelessCaptureInterface iface;
	private final long realm;
	
	public WifiDaemonInterface(IWirelessCaptureInterface iface, long realm) {
		this.iface = iface;
		this.realm = realm;
	}
	
	public IWirelessCaptureInterface getInterface() {
		return iface;
	}
	
	public long getRealm() {
		return realm;
	}
	
	public String toString() {
		return iface.getName();
	}
}
