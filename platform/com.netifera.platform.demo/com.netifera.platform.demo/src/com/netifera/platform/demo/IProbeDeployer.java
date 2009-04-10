package com.netifera.platform.demo;

import java.nio.channels.SocketChannel;

public interface IProbeDeployer {
	 void deployProbe(SocketChannel socket, long realmId, long spaceId);
}
