package com.netifera.platform.api.channels;

import com.netifera.platform.api.dispatcher.IClientDispatcher;
import com.netifera.platform.api.dispatcher.IServerDispatcher;

public interface IChannelFactory {
	IChannelConnecter createConnecter(IClientDispatcher clientDispatcher, String channelConfig);
	IChannelServer createServer(IServerDispatcher serverDispatcher, String channelConfig);
	String getType();
}
