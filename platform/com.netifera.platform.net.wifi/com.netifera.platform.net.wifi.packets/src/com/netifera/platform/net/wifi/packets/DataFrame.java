package com.netifera.platform.net.wifi.packets;

import com.netifera.platform.util.addresses.MACAddress;

public class DataFrame extends WiFiFrame {
	MACAddress address4;

	@Override
	protected void unpackHeader() {
		super.unpackHeader();
		if(toDS && fromDS) {
			address4 = new MACAddress(unpackBytes(6));
		}
	}
	
	@Override
	public int headerLength() {
		if (toDS && fromDS) {
			return super.headerLength()+6;
		}
		return super.headerLength();
	}
	
	@Override
	public int nextProtocol() {
		if (type >= 0x24) {
			return -1; // no data
		} else {
			return 0; // IPv4
		}
	}
	
	public MACAddress destination() {
		if(!toDS && !fromDS) {
			return address1;
		} else if(!toDS && fromDS) {
			return address1;
		}
		return address3;
	}

	public MACAddress source() {
		if(!toDS && fromDS) {
			return address3;
		} else if(toDS && fromDS) {
			return address4;
		}
		return address2;
	}
	
	public MACAddress bssid() {
		if(!toDS && !fromDS) {
			return address3;
		} else if(!toDS && fromDS) {
			return address2;
		} else if(toDS && !fromDS) {
			return address1;
		}
		return null;
	}
}
