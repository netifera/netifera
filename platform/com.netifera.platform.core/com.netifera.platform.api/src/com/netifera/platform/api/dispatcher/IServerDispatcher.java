package com.netifera.platform.api.dispatcher;

import com.netifera.platform.api.channels.IChannelTransport;

public interface IServerDispatcher extends IMessageDispatcher {
	void registerNewConnection(IChannelTransport transport);
}
