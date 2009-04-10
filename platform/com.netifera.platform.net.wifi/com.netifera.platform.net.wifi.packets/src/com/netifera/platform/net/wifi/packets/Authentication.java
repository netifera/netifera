package com.netifera.platform.net.wifi.packets;

public class Authentication extends ManagementFrame {
	int authenticationAlgorithmNumber;
	int authenticationSequenceNumber;
	int statusCode;
	
	@Override
	protected void unpackFixedLengthFields() {
		authenticationAlgorithmNumber = unpack16();
		authenticationSequenceNumber = unpack16();
		statusCode = unpack16();
	}

	protected int minimumHeaderLength() {
		return super.minimumHeaderLength()+6;
	}
}
