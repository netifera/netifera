package com.netifera.platform.channel.tcplisten;

import java.net.InetSocketAddress;

import com.netifera.platform.api.channels.IChannelConnecter;

public interface ITCPListenConnecter extends IChannelConnecter {

	void setAddress(InetSocketAddress address);

}
