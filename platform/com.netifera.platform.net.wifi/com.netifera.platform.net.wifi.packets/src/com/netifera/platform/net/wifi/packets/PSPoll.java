package com.netifera.platform.net.wifi.packets;

import com.netifera.platform.util.addresses.MACAddress;

public class PSPoll extends ControlFrame {
	@Override
	protected void unpackAddresses() {
		address1 = new MACAddress(unpackBytes(6));
		address2 = new MACAddress(unpackBytes(6));
	}
	
	@Override
	protected int minimumHeaderLength() {
		return super.minimumHeaderLength()-6;
	}
	
	public MACAddress bssid() {
		return address1;
	}
	
	public int aid() {
		return duration;
	}
}
