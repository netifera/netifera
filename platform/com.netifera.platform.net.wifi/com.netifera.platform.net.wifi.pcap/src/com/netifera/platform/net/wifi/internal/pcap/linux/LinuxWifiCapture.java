package com.netifera.platform.net.wifi.internal.pcap.linux;

import com.netifera.platform.api.system.ISystemService;
import com.netifera.platform.net.pcap.IPacketCapture;
import com.netifera.platform.net.wifi.internal.pcap.IWifiNative;

public class LinuxWifiCapture implements IWifiNative {

	@SuppressWarnings("unused")
	private final ISystemService system;
	@SuppressWarnings("unused")
	private final IPacketCapture pcap;
	
	public LinuxWifiCapture(ISystemService system, IPacketCapture pcap) {
		this.system = system;
		this.pcap = pcap;
	}
	public boolean enableMonitorMode(boolean enable) {
		return false;
	}

	public boolean setChannel(int channel) {
		return false;
	}

}
