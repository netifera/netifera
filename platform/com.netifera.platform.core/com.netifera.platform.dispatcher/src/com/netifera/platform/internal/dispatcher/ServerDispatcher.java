package com.netifera.platform.internal.dispatcher;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.netifera.platform.api.channels.IChannelMessageSerializer;
import com.netifera.platform.api.channels.IChannelTransport;
import com.netifera.platform.api.dispatcher.IMessageHandler;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.dispatcher.IServerDispatcher;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;

public class ServerDispatcher implements IServerDispatcher {

	private final MessageDispatcher dispatcher;
	private final ILogger logger;
	private final Set<IMessenger> messengers;

	ServerDispatcher(ILogManager logManager) {
		logger = logManager.getLogger("Dispatcher [server]");
		dispatcher = new MessageDispatcher(logger);
		messengers = new HashSet<IMessenger>();
	}
	
	public void registerNewConnection(IChannelTransport transport) {	
		try {
			messengers.add(dispatcher.createMessenger(transport));
		} catch (IOException e) {
			logger.error("IO Error registering new connection", e);
		}	
	}
	
	public void registerLocalConnection(IChannelMessageSerializer localSerializer) {
		final Messenger messenger = new Messenger(localSerializer, this, logger);
		messenger.start();
		messengers.add(messenger);
	}
	
	public Iterable<IMessenger> getActiveMessengers() {
		return Collections.unmodifiableSet(messengers);
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
