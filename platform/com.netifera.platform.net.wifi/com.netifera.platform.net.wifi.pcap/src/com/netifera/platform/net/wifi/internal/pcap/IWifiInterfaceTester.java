package com.netifera.platform.net.wifi.internal.pcap;

import com.netifera.platform.net.pcap.ICaptureInterface;

public interface IWifiInterfaceTester {
	boolean isWifiDevice(ICaptureInterface iface);
}
