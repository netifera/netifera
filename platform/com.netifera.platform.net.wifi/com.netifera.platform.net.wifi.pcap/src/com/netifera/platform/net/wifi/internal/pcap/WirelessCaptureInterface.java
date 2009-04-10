package com.netifera.platform.net.wifi.internal.pcap;

import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.pcap.IPacketCapture;
import com.netifera.platform.net.pcap.IPacketHandler;
import com.netifera.platform.net.wifi.pcap.IWifiPacketCapture;
import com.netifera.platform.net.wifi.pcap.IWifiPacketCaptureFactory;
import com.netifera.platform.net.wifi.pcap.IWirelessCaptureInterface;

public class WirelessCaptureInterface implements IWirelessCaptureInterface {

	final private ICaptureInterface pcapInterface;
	final private IWifiPacketCaptureFactory wifiFactory;
	
	WirelessCaptureInterface(IWifiPacketCaptureFactory wifiPcapFactory, ICaptureInterface iface) {
		this.pcapInterface = iface;
		this.wifiFactory = wifiPcapFactory;
	}
	
	public boolean isMonitorModeCapable() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean captureAvailable() {
		return pcapInterface.captureAvailable();
	}

	public String getName() {
		return pcapInterface.getName();
	}
	
	
	public ICaptureInterface getInterface() {
		return pcapInterface;
	}
	
	public String toString() {
		return "Wireless: " + pcapInterface.toString();
	}
	public IPacketCapture pcapCreate(int snaplen, boolean promiscuous,
			int timeout, IPacketHandler handler) {
		final IWifiPacketCapture capture = wifiFactory.create(this, snaplen, promiscuous, timeout, handler);
		capture.enableMonitorMode(true);
		return capture;
	}

}
