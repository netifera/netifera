package com.netifera.platform.net.wifi.packets;

public class AssociationResponse extends ManagementFrame {
	int capabilityInformation;
	int statusCode;
	int associationId;
	
	@Override
	protected void unpackFixedLengthFields() {
		capabilityInformation = unpack16();
		statusCode = unpack16();
		associationId = unpack16();
	}

	protected int minimumHeaderLength() {
		return super.minimumHeaderLength()+6;
	}
}
