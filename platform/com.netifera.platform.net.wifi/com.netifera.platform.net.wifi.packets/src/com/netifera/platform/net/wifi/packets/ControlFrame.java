package com.netifera.platform.net.wifi.packets;

public class ControlFrame extends WiFiFrame {

	protected void unpackSequenceControlField() {
		// do nothing
	}
	
	protected int minimumHeaderLength() {
		return super.minimumHeaderLength()-2; // no sequence control field
	}
}
