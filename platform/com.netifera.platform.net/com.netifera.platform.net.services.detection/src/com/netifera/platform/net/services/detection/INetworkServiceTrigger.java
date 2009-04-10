package com.netifera.platform.net.services.detection;

import com.netifera.platform.util.PortSet;

public interface INetworkServiceTrigger {
	String getProtocol();
	PortSet getPorts();
	byte[] getBytes();
}
