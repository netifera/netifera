package com.netifera.platform.net.wifi.packets;

public class ProbeResponse extends ManagementFrame {
	long timestamp;
	int beaconInterval;
	CapabilityInformation capabilities;
	
	protected void unpackFixedLengthFields() {
		timestamp = unpack64();
		beaconInterval = unpack16();
		capabilities = new CapabilityInformation((short) unpack16());
	}

	@Override
	protected int minimumHeaderLength() {
		return super.minimumHeaderLength()+12;
	}
	
	public CapabilityInformation capabilities() {
		return capabilities;
	}
}
