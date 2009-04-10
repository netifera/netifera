package com.netifera.platform.net.internal.daemon.sniffing;

import com.netifera.platform.net.pcap.ICaptureInterface;

public class SniffingDaemonInterface {
	private final ICaptureInterface iface;
	private final long realm;
	
	public SniffingDaemonInterface(ICaptureInterface iface, long realm) {
		this.iface = iface;
		this.realm = realm;
	}
	
	public ICaptureInterface getInterface() {
		return iface;
	}
	
	public long getRealm() {
		return realm;
	}
	
	@Override
	public String toString() {
		return iface.getName();
	}

}
