package com.netifera.platform.net.wifi.packets;

public class CapabilityInformation {
	private final int value;
	
	public CapabilityInformation(short value) {
		this.value = (value & 0xFFFF);
	}
	
	public boolean ess() {
		return getBit(0);
	}
	
	public boolean ibss() {
		return getBit(1);
	}
	
	public boolean privacy() {
		return getBit(4);
	}
	
	private boolean getBit(int index) {
		return (value & (1 << (index))) != 0;
	}
}
