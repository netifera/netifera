package com.netifera.platform.net.daemon.sniffing.model;

public interface ISniffingEntityFactory {
	SniffingSessionEntity createSniffingSession(long realm, long space);
	CaptureFileEntity createCaptureFile(long realm, long space, String path);
	NetworkInterfaceEntity createNetworkInterface(long realm, long space, String name);
}
