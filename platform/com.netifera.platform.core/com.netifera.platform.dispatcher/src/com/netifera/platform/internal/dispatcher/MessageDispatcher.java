package com.netifera.platform.internal.dispatcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.netifera.platform.api.channels.IChannelMessageSerializer;
import com.netifera.platform.api.channels.IChannelTransport;
import com.netifera.platform.api.dispatcher.DispatchException;
import com.netifera.platform.api.dispatcher.IMessageDispatcher;
import com.netifera.platform.api.dispatcher.IMessageHandler;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.dispatcher.StatusMessage;
import com.netifera.platform.internal.dispatcher.channels.ChannelMessageSerializer;

/*
 * Dispatcher for registering message handlers and delivering incoming <code>ProbeMessage</code>
 * objects to those handlers.
 */
public class MessageDispatcher implements IMessageDispatcher {

	/*
	 * Map named type to message handler.
	 */
	private final Map<String, IMessageHandler> messageMap = new HashMap<String, IMessageHandler>();
	
	private final ILogger logger;
	
	MessageDispatcher(ILogger logger) {
		this.logger = logger;
	}
	
	public void registerMessageHandler(String messageName, IMessageHandler handler) {
		synchronized(messageMap) {
			messageMap.put(messageName, handler);
		}
		
		logger.debug("Registering handler for message: " + messageName);
	}

	public void dispatch(IMessenger source, IProbeMessage message) throws MessengerException {
		
		logger.debug("Message " + message.getNamedType() + " seq: " + message.getSequenceNumber());
		
		IMessageHandler handler;
		synchronized(messageMap) {
			handler = messageMap.get(message.getNamedType());
		}
		
		if(handler == null) {
			logger.warning("Unhandled message type "+message.getNamedType()+" received (" + message.toString() + ")");
			source.emitMessage(StatusMessage.createUnhandledMessageError(message.getSequenceNumber()));
			return;
		}

		try {
			handler.call(source, message);
		} catch(DispatchException e) {
			logger.warning("Error dispatching message " + message.getNamedType() + ": " + e.getMessage());
		}
		catch(Exception e) {
			logger.error("Unhandled exception dispatching message " + message.getNamedType(), e);
		}
	}
	
	public Set<String> messagesHandled() {
		return messageMap.keySet();
	}
	
	Messenger createMessenger(IChannelTransport transport) throws IOException {
		logger.debug("Creating messenger for channel transport: " + transport);
		
		if(!transport.isConnected()) {
			throw new IllegalStateException("Cannot create messenger on closed channel");
		}
		
		final IChannelMessageSerializer serializer = ChannelMessageSerializer.createClientSerializer(transport, logger);		
		final Messenger m = new Messenger(serializer, this, logger);
		m.start();
		return m;
	}
	

}
