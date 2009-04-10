package com.netifera.platform.channel.tcplisten;

import com.netifera.platform.api.channels.IChannelServer;

public interface ITCPListenServer extends IChannelServer {
	public final static String CHANNEL_TYPE = "tcplisten";

	void setListenPort(int port);
}
