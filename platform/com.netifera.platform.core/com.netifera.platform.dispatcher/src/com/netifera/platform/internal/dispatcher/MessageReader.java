package com.netifera.platform.internal.dispatcher;

import java.util.HashMap;
import java.util.Map;

import com.netifera.platform.api.channels.ChannelException;
import com.netifera.platform.api.channels.IChannelMessageSerializer;
import com.netifera.platform.api.dispatcher.IMessageDispatcher;
import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.log.ILogger;

/**
 * Create a thread which loops reading <code>ProbeMessage</code> objects from a 
 * channel and delivering them to threads waiting for responses to sent messages or
 * to a general message dispatcher.  Response messages can be retrieved by sequence
 * number by calling the <code>#readResponse(int)</code> method.
 * @author mike
 *
 */
class MessageReader extends Thread {
	
	/**
	 * Default time to wait for message response
	 * @see #setReadTimeout(long)
	 * @see #readResponse(int)
	 * @see IMessenger#exchangeMessage(ProbeMessage)
	 * 
	 */
	private static final long DEFAULT_READ_TIMEOUT_MILLISECONDS = 50000;
	
	/**
	 * Stores incoming response messages.  The key is the message sequence
	 * number.
	 */
	private Map<Integer, IProbeMessage> responseMap;
	
	/**
	 * Currently configured timeout for reading responses.
	 * @see #setReadTimeout(long)
	 * @see #readResponse(int)
	 * @see IMessenger#exchangeMessage(ProbeMessage)
	 */
	private long readTimeout;
	
	/**
	 * Incoming messages which are not response messages are sent to the dispatcher.
	 */
	private IMessageDispatcher dispatcher;
	
	/**
	 * Reference to the <code>Messenger</code> this <code>MessageReader</code> belongs to
	 */
	private final Messenger messenger;
	
	/* serializer to read individual probe messages from */
	private final IChannelMessageSerializer serializer;
	
	private final ILogger logger;
	/**
	 * Create a new message reader without starting it.
	 * @param channel The channel to read messages from.
	 */
	MessageReader(IChannelMessageSerializer serializer, Messenger messenger, ILogger logger) {
		setName("Probe Message Reading Thread");
		setDaemon(true);
	
		this.serializer = serializer;
		this.messenger = messenger;
		this.logger = logger;
	
		
		responseMap = new HashMap<Integer, IProbeMessage>();
		readTimeout = DEFAULT_READ_TIMEOUT_MILLISECONDS;
		
	}
	
	/**
	 * Change timeout to wait for response messages
	 * @see IMessenger#exchangeMessage(ProbeMessage)
	 * @param milliseconds New timeout in milliseconds.
	 */
	void setReadTimeout(long milliseconds) {
		readTimeout = milliseconds;
	}
	
	/**
	 * Set a <code>ProbeMessageDispatcher</code> to use for dispatching messages.
	 * @param dispatcher The new dispatcher.
	 */
	void setDispatcher(IMessageDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	
	/**
	 * Wait for a response to a particular message.
	 * @see IMessenger#exchangeMessage(ProbeMessage)
	 * @param sequenceNumber The sequence number of the message to wait for a response to.
	 * @return The response message.
	 * @throws Exception on fatal errors
	 */
	IProbeMessage readResponse(int sequenceNumber) throws MessengerException {
		long start = System.currentTimeMillis();
	
		synchronized(responseMap) {
			while(responseMap.containsKey(sequenceNumber) == false) {
				
				long elapsed = System.currentTimeMillis() - start;
				
			
				if(elapsed >= readTimeout) {
					markAbandoned(sequenceNumber);
					throw new MessengerException("Timeout reading response message");
				}
				
				if(isAlive() == false) {
					/* probably redundant */
					markAbandoned(sequenceNumber);
					throw new MessengerClosedException();
				}
				
				try {
					responseMap.wait(readTimeout-elapsed);
				} catch (InterruptedException e) {
					markAbandoned(sequenceNumber);
					Thread.currentThread().interrupt();
					// XXX should be chained?
					throw new MessengerException("Interrupted while reading response");

				}
			}

			return responseMap.remove(sequenceNumber);
				
		}
		
		
		
	}
	/**
	 * Put a null entry in the map to indicate to the reader that we are not
	 * going to wait for this message.
	 * @see #isAbandoned(int)
	 * @param sequenceNumber The sequence number of the abandoned response.
	 */
	private void markAbandoned(int sequenceNumber) {
		responseMap.put(sequenceNumber, null);
	}
	
	/**
	 * Main message reader loop.
	 */
	public void run() {
		logger.debug("Starting message reader");
		
		while(!interrupted()) {
			processOneMessage();
		}
		
		responseMap.clear();
		messenger.close();
	}
	
	/**
	 * Process one message and either place it into the <code>responseMap</code> for a waiting thread
	 * or send it to the dispatcher.
	 */
	private void processOneMessage() {
		IProbeMessage message;
	
		try {
			message = serializer.readMessage();
		} catch (ChannelException e) {
			interrupt();
			return;
		} catch(Exception e) {
			logger.error("Error reading message", e);
			return;
		}
		
		if(message.isResponse()) {
			
			
			synchronized(responseMap) {
				
				if(isAbandoned(message.getSequenceNumber())) {
					return;
				}
				
				responseMap.put(message.getSequenceNumber(), message);
				responseMap.notifyAll();
				return;
			}
		}
		
		if(dispatcher != null) {
			try {
				dispatcher.dispatch(messenger, message);
			} catch (MessengerException e) {
				e.printStackTrace();
			}
		}
		
	}
	/**
	 * The thread waiting for a message response must place a <code>null</code> value
	 * in the sequence number slot if it decides to not wait for the response.  This
	 * avoids leaking messages into the responseMap.  This function detects an abandoned
	 * response and removes the <code>null</code> sentinel mapping.
	 * @see #markAbandoned(int)
	 * @param sequenceNumber The sequence number of the response to check.
	 * @return Returns true if abandoned.
	 */
	private boolean isAbandoned(int sequenceNumber) {
		if(responseMap.containsKey(sequenceNumber)) {
			IProbeMessage old = responseMap.remove(sequenceNumber);
			assert(old == null);
			return true;
		}
		
		return false;
	}
	



}
