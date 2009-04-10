package com.netifera.platform.api.dispatcher;

import java.io.IOException;

import com.netifera.platform.api.channels.IChannelTransport;

public interface IClientDispatcher extends IMessageDispatcher {	
	IMessenger createMessenger(IChannelTransport transport) throws IOException;
	IMessenger getLocalMessenger();
}
