package com.netifera.platform.api.dispatcher;

import com.netifera.platform.api.probe.IProbe;


/**
 * An <code>IMessenger</code> instance is a handle for exchanging messages with a probe over
 * a previously established communication channel.  
 * 
 *
 */
public interface IMessenger {
	/**
	 * Send a <code>ProbeMessage</code> without waiting for a reply.  Used for asynchronous
	 * notification and to send message responses in message handler callbacks.
	 * @param message Message to send.
	 * @throws MessengerException Indicates failure to send message
	 */
	void emitMessage(IProbeMessage message) throws MessengerException;
	
	/**
	 * XXX fix this comment
	 * 
	 * This is a convenience method for message types which have a simple <code>StatusMessage</code>
	 * return type.  It is equivalent to calling {@link #exchangeMessage(ProbeMessage)} and throwing
	 * a <code>MessengerException</code> if the response type is not <code>StatusMessage</code>.  
	 * @param message The message to send.
	 * @return StatusMessage object received as response.
	 * @throws MessengerException Failure in exchanging messages or response was not 
	 * <code>instanceof StatusMessage</code>
	 */
	void sendMessage(IProbeMessage message) throws MessengerException;
	
	/**
	 * Send a <code>ProbeMessage</code> and expect a matching reply from the
	 * other side.
	 * @param message Message to send.
	 * @return Message received in response, or <code>null</code> on failure.
	 * @throws MessengerException 
	 */
	IProbeMessage exchangeMessage(IProbeMessage message) throws MessengerException;

	/**
	 * Tell the messenger to shut down.
	 */
	void close();
	
	boolean isOpen();
	/**
	 * This will return null if called on a server messenger, otherwise returns the probe
	 * associated with this connection.
	 */
	IProbe getProbe();
	void setProbe(IProbe probe);
	
	void respondOk(IProbeMessage message) throws MessengerException;
	
	void respondError(IProbeMessage message, String errorMessage) throws MessengerException;
}
