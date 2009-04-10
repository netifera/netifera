package com.netifera.platform.net.wifi.internal.sniffing.daemon.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class StartWifiSniffer extends ProbeMessage {

	private static final long serialVersionUID = -583418244804509945L;
	public final static String ID = "StartWifiSniffer";
	private final long spaceId;
	
	public StartWifiSniffer(long spaceId) {
		super(ID);
		this.spaceId = spaceId;
	}
	
	public long getSpaceId() {
		return spaceId;
	}
}
