package com.netifera.platform.internal.dispatcher;

import java.io.IOException;
import java.util.Set;

import com.netifera.platform.api.channels.IChannelTransport;
import com.netifera.platform.api.dispatcher.IClientDispatcher;
import com.netifera.platform.api.dispatcher.IMessageHandler;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.internal.dispatcher.channels.LocalChannel;

public class ClientDispatcher implements IClientDispatcher {

	private final MessageDispatcher dispatcher;
	private final ServerDispatcher serverDispatcher;
	private final ILogger logger;
	private LocalChannel localChannel;
	
	ClientDispatcher(ServerDispatcher serverDispatcher, ILogManager logManager) {
		logger = logManager.getLogger("Dispatcher [client]");
		dispatcher = new MessageDispatcher(logger);
		this.serverDispatcher = serverDispatcher;
	}
	
	public IMessenger createMessenger(IChannelTransport transport) throws IOException {
		return dispatcher.createMessenger(transport);
	}

	public IMessenger getLocalMessenger() {
		if(localChannel == null) {
			localChannel = LocalChannel.create(this, serverDispatcher, logger);
		}
		return localChannel.getClientMessenger();
	}
	public void dispatch(IMessenger source, IProbeMessage message)
			throws MessengerException {
		dispatcher.dispatch(source, message);		
	}

	public Set<String> messagesHandled() {
		return dispatcher.messagesHandled();
	}

	public void registerMessageHandler(String messageName,
			IMessageHandler handler) {
		dispatcher.registerMessageHandler(messageName, handler);		
	}

}
