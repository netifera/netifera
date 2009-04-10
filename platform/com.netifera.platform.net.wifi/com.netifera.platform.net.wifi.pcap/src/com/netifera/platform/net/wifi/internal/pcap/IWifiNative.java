package com.netifera.platform.net.wifi.internal.pcap;

public interface IWifiNative {
	boolean enableMonitorMode(boolean enable);
	boolean setChannel(int channel);


}
