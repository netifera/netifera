package com.netifera.platform.net.services.detection;

import java.nio.ByteBuffer;
import java.util.Map;

public interface INetworkServiceDetectorService {
	Map<String,String> detect(String protocol, int port, String trigger, String response);
	Map<String,String> detect(String protocol, int port, ByteBuffer triggerBuffer, ByteBuffer responseBuffer);
}
