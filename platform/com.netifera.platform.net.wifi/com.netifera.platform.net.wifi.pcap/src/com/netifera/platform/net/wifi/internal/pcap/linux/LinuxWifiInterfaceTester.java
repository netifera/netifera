package com.netifera.platform.net.wifi.internal.pcap.linux;

import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.wifi.internal.pcap.IWifiInterfaceTester;

public class LinuxWifiInterfaceTester implements IWifiInterfaceTester {

	public boolean isWifiDevice(ICaptureInterface iface) {
		return false;
	}

}
