package com.netifera.platform.api.dispatcher;




/**
 * Callback for processing a received <code>ProbeMessage</code>
 * 
 */
public interface IMessageHandler {
	/**
	 * Callback for <code>ProbeMessage</code>
	 * @param messenger Refers to the connection the message was received on so 
	 * that the callback can send replies.
	 * @param message Message to process.
	 */
	void call(IMessenger messenger, IProbeMessage message) throws DispatchException;

}
