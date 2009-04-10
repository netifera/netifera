package com.netifera.platform.net.sniffing.util;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.sniffing.ISniffingEngineService;

/**
 * This interface provides access to some internal methods for use by internal
 * components.
 */
public interface ISniffingEngineEx extends ISniffingEngineService {
	
	IBasicInterfaceManager createInterfaceManager(IPacketSource packetManager);
	
	ILogger getLogger();
	
	void removeCaptureFileInterface(CaptureFileInterface iface);
}
