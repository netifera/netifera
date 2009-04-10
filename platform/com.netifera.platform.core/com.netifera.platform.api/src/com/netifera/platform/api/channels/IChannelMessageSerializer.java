package com.netifera.platform.api.channels;

import com.netifera.platform.api.dispatcher.IProbeMessage;

public interface IChannelMessageSerializer {
	IProbeMessage readMessage() throws ChannelException;
	void sendMessage(IProbeMessage message) throws ChannelException;
	void close();
}
