package com.netifera.platform.net.wifi.internal.sniffing.daemon.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class StopWifiSniffer extends ProbeMessage {
	
	private static final long serialVersionUID = -7862977851901142607L;
	public final static String ID = "StopWifiSniffer";
	
	public StopWifiSniffer() {
		super(ID);
	}

}
