package com.netifera.platform.channel.tcpconnect;

import java.net.InetSocketAddress;

import com.netifera.platform.api.channels.IChannelServer;

public interface ITCPConnectServer extends IChannelServer {
	public final static String CHANNEL_TYPE = "connectback";

	void setAddress(InetSocketAddress address);


}
