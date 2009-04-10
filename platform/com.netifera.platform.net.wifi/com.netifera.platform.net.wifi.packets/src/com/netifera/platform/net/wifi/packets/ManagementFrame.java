package com.netifera.platform.net.wifi.packets;

import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.util.addresses.MACAddress;

public abstract class ManagementFrame extends WiFiFrame {
	public final static int WLAN_EID_SSID			= 0;
	public final static int WLAN_EID_SUPP_RATES		= 1;
	public final static int WLAN_EID_DS_PARAMS		= 3;
	public final static int WLAN_EID_RSN			= 48;
	public final static int WLAN_EID_EXT_SUPP_RATES = 50;

	private List<InformationElement> informationElements;
	
	@Override
	protected void unpackHeader() {
		super.unpackHeader();
		unpackFixedLengthFields();
		unpackInformationElements();
	}
	
	abstract protected void unpackFixedLengthFields();
	
	private void unpackInformationElements() {
		informationElements = new ArrayList<InformationElement>();
		boolean foundSSID = false;
		while (remaining() > 4) {
			InformationElement element = unpackInformationElement();
			if(element == null)
				break;
			if (element.id() == 0) {
				if (foundSSID) return;
					foundSSID = true;
				}
			informationElements.add(element);
		}
	}
	
	private InformationElement unpackInformationElement() {
		int id = unpack8();
		int length = unpack8();
		if(remaining() < length)
			return null;
		return new InformationElement(id, unpackBytes(length));
	}
	
	public MACAddress destination() {
		return receiver();
	}
	
	public MACAddress source() {
		return transmitter();
	}
	
	public MACAddress bssid() {
		return address3;
	}
	
	public List<InformationElement> informationElements() {
		return informationElements;
	}
	
	public InformationElement findInformationElement(int id) {
		for(InformationElement e : informationElements) {
			if(e.id() == id) return e;
		}
		return null;
	}
	
	public String stringInformationElement(int id) {
		InformationElement e = findInformationElement(id);
		if(e != null) {
			return e.toString();
		} else return null;
		
	}
	
	public Integer integerInformationElement(int id) {
		InformationElement e = findInformationElement(id);
		if(e != null) {
			return e.toInteger();
		} else return null;
	}
	
	public byte[] rawInformationElement(int id) {
		InformationElement e = findInformationElement(id);
		if(e != null) {
			return e.toBytes();
		} else return null;
	}
	
	public String ssid() {
		return stringInformationElement(WLAN_EID_SSID);
	}
	
	public Integer channel() {
		return integerInformationElement(WLAN_EID_DS_PARAMS);
	}
}
