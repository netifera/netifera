package com.netifera.platform.api.channels;

public interface IChannelConnecter {
	void connect(IChannelConnectProgress progress);
	void abortConnect();
}
