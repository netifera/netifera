package com.netifera.platform.net.wifi.packets;

public class AssociationRequest extends ManagementFrame {
	int capabilityInformation;
	int listenInterval;
	
	protected void unpackFixedLengthFields() {
		capabilityInformation = unpack16();
		listenInterval = unpack16();
	}

	protected int minimumHeaderLength() {
		return super.minimumHeaderLength()+4;
	}
}
