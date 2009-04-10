package com.netifera.platform.net.wifi.pcap;

import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.pcap.ex.ICaptureInterfaceEx;

public interface IWirelessCaptureInterface extends ICaptureInterfaceEx {
	boolean isMonitorModeCapable();
	ICaptureInterface getInterface();

}
