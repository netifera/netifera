package com.netifera.platform.net.wifi.packets;

import com.netifera.platform.util.addresses.MACAddress;


public class Ack extends ControlFrame {
	@Override
	protected void unpackAddresses() {
		address1 = new MACAddress(unpackBytes(6));
	}
	
	@Override
	protected int minimumHeaderLength() {
		return super.minimumHeaderLength()-12;
	}
}
