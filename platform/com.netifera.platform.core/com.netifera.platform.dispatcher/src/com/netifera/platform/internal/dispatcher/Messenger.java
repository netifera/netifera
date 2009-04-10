package com.netifera.platform.internal.dispatcher;

import com.netifera.platform.api.channels.IChannelMessageSerializer;
import com.netifera.platform.api.dispatcher.IMessageDispatcher;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.dispatcher.MessageErrorException;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.dispatcher.ProbeMessage;
import com.netifera.platform.api.dispatcher.UnhandledMessageException;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.dispatcher.StatusMessage;

/*
 * <code>IMessenger</code> implementation.
 *
 */
public class Messenger implements IMessenger {

	private final IChannelMessageSerializer serializer;
	private final MessageReader reader;
	private final MessageSender sender;
	private volatile boolean closed;
	
	private ILogger logger;
	private IProbe probe;
	
	
	public Messenger(IChannelMessageSerializer serializer, IMessageDispatcher dispatcher, ILogger logger) {
		this.serializer = serializer;
		reader = new MessageReader(serializer, this, logger);
		if(dispatcher != null) {
			reader.setDispatcher(dispatcher);
		}
		sender = new MessageSender(serializer, this, logger);
		closed = false;
		this.logger = logger;
	}
	
	public synchronized void close() {
		if(closed) return;
		closed = true;
		reader.interrupt();
		sender.interrupt();
		serializer.close();
		if(probe != null) {
			probe.setDisconnected();
		}
	}
	
	public boolean isOpen() {
		return !closed;
	}
	public void start() {
		reader.start();
		sender.start();
	}
	
	public IProbe getProbe() {
		return probe;
	}
	
	public void setProbe(IProbe probe) {
		this.probe = probe;
	}

	public IProbeMessage exchangeMessage(IProbeMessage message) throws MessengerException {
		if(closed) {
			throw new MessengerClosedException();
		}
		sender.queueMessage((ProbeMessage)message);
		return reader.readResponse(message.getSequenceNumber());
		
	}

	public void sendMessage(IProbeMessage message) throws MessengerException {
		logger.debug("Sending probe message: " + message.getClass());
		IProbeMessage response = exchangeMessage(message);
		
		
		if(response instanceof StatusMessage) {
			StatusMessage status = (StatusMessage) response;
			if(status.isError()) {
				if(status.isUnhandledMessageError()) {
					throw new UnhandledMessageException();
				} else {
					throw new MessageErrorException(status.getErrorMessage());
				}
			}

		}
		else {
			logger.debug("StatusMessage not received");
			throw new MessengerException("Did not receive expected StatusMessage response");
		}
		
	}
	
	public String toString() {
		return "Messenger for [" + probe + "]";
	}
	
	public void emitMessage(IProbeMessage message) throws MessengerException {
		if(closed) {
			throw new MessengerClosedException();
		}
		
		sender.queueMessage((ProbeMessage)message);
	}
	
	public void respondOk(IProbeMessage message) throws MessengerException {
		emitMessage(StatusMessage.createOk(message.getSequenceNumber()));
		
	}
	
	public void respondError(IProbeMessage message, String errorMessage) throws MessengerException {
		emitMessage(StatusMessage.createError(message.getSequenceNumber(), errorMessage));
		
	}
		

}
