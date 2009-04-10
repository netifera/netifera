package com.netifera.platform.net.services.detection;

import java.util.Map;

import com.netifera.platform.util.PortSet;

public interface INetworkServiceDetector {
	String getProtocol();
	PortSet getPorts();
	Map<String, String> detect(String clientData, String serverData);
}
