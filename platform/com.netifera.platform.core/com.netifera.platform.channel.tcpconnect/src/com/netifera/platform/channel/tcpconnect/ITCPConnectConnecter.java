package com.netifera.platform.channel.tcpconnect;

import com.netifera.platform.api.channels.IChannelConnecter;

public interface ITCPConnectConnecter extends IChannelConnecter {
	public final static String CHANNEL_TYPE = "connectback";

	void setConnectbackPort(int port);

}
