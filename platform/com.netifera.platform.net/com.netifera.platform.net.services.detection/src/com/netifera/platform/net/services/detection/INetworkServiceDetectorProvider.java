package com.netifera.platform.net.services.detection;

import java.util.List;

public interface INetworkServiceDetectorProvider {
	List<INetworkServiceTrigger> getTriggers();
	List<INetworkServiceDetector> getServerDetectors();
	List<INetworkServiceDetector> getClientDetectors();
}
