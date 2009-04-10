package com.netifera.platform.net.wifi.internal.pcap;

import java.util.List;

import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.pcap.Datalink;
import com.netifera.platform.net.pcap.IPacketCapture;
import com.netifera.platform.net.wifi.decoders.PrismDecoder;
import com.netifera.platform.net.wifi.decoders.RadioTapDecoder;
import com.netifera.platform.net.wifi.decoders.WiFiDecoder;
import com.netifera.platform.net.wifi.pcap.IWifiPacketCapture;

public class WifiPacketCapture implements IWifiPacketCapture {
	private final static IPacketDecoder wifiDecoder = new WiFiDecoder();
	private final static IPacketDecoder prismDecoder = new PrismDecoder();
	private final static IPacketDecoder radiotapDecoder = new RadioTapDecoder();
	
	private final IPacketCapture packetCapture;
	private final IWifiNative wifiNative;
	
	WifiPacketCapture(IPacketCapture pcap, IWifiNative wifiNative) {
		packetCapture = pcap;
		this.wifiNative = wifiNative;
	}
	
	public boolean enableMonitorMode(boolean enable) {
		return wifiNative.enableMonitorMode(enable);
	}

	public boolean setChannel(int channel) {
		return wifiNative.setChannel(channel);
	}

	public void close() {
		packetCapture.close();
	}

	public String getLastError() {
		return packetCapture.getLastError();
	}

	public Datalink getLinkType() {
		return packetCapture.getLinkType();
	}

	public boolean open() {
		if(!packetCapture.open()) {
			return false;
		}
		
		if(!wifiNative.enableMonitorMode(true)) {
			packetCapture.close();
			return false;
		}
		
		return true;
	}

	public boolean read() {
		return packetCapture.read();
	}

	public int getFileDescriptor() {
		return packetCapture.getFileDescriptor();
	}

	public List<Datalink> getDltList() {
		return packetCapture.getDltList();
	}

	public boolean setDataLink(Datalink dlt) {
		return packetCapture.setDataLink(dlt);
	}

	public void setError(String error) {
		packetCapture.setError(error);		
	}

	public IPacketDecoder getDecoder() {
		System.out.println("DLT = " + packetCapture.getLinkType());
		switch(packetCapture.getLinkType()) {
		case DLT_IEEE802:
		case DLT_IEEE802_11:
			return wifiDecoder;
		case DLT_IEEE802_11_RADIO_AVS:
			return prismDecoder;
		case DLT_IEEE802_11_RADIO:
			return radiotapDecoder;
		}
		return packetCapture.getDecoder();
	}

}
