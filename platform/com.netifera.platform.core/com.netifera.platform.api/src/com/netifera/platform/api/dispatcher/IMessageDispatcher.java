package com.netifera.platform.api.dispatcher;

import java.util.Set;


public interface IMessageDispatcher {

	/**
	 * Register a new message type to be handled.
	 * @param messageName Named type of message.
	 * @param handler <code>IMessageHandler</code> object to handle messages of this type.
	 */
	 void registerMessageHandler(String messageName,
			IMessageHandler handler);

	/**
	 * Dispatch a message by searching the registered handlers for a match and calling it.
	 * This method should probably do something to indicate that it could not find a handler 
	 * for the message.
	 * @param source Source the message was received from.  Passed to handler functions so
	 * the can reply.
	 * @param message The message to deliver.
	 * 
	 */
	void dispatch(IMessenger source, IProbeMessage message) throws MessengerException;

	/**
	 * Return a {@code Set<String>} of named message types which are registered with
	 * the dispatcher.
	 */
	Set<String> messagesHandled();
	
}