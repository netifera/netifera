package com.netifera.platform.net.services.detection;

import com.netifera.platform.util.PortSet;

public interface IServerDetectorService extends INetworkServiceDetectorService {
	byte[] getTrigger(String protocol, int port);
	PortSet getTriggerablePorts(String protocol);
}
