package com.netifera.platform.net.wifi.packets;


public class Beacon extends ManagementFrame {
	long timestamp;
	int beaconInterval;
	CapabilityInformation capabilities;
	
	@Override
	protected void unpackFixedLengthFields() {
		timestamp = unpack64();
		beaconInterval = swap16(unpack16());
		capabilities = new CapabilityInformation((short) swap16(unpack16()));
	}
	
	@Override
	protected int minimumHeaderLength() {
		return super.minimumHeaderLength()+12;
	}
	
	public CapabilityInformation capabilities() {
		return capabilities;
	}
}
