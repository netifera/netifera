package com.netifera.platform.net.wifi.packets;

import com.netifera.platform.util.addresses.MACAddress;

public class ReassociationRequest extends AssociationRequest {
	MACAddress currentAddress;
	
	@Override
	protected void unpackFixedLengthFields() {
		super.unpackFixedLengthFields();
		currentAddress = new MACAddress(unpackBytes(6));
	}

	@Override
	protected int minimumHeaderLength() {
		return super.minimumHeaderLength()+6;
	}
}
