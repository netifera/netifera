package com.netifera.platform.internal.dispatcher;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.netifera.platform.api.channels.ChannelException;
import com.netifera.platform.api.channels.IChannelMessageSerializer;
import com.netifera.platform.api.dispatcher.ProbeMessage;
import com.netifera.platform.api.log.ILogger;
/**
 * Creates a thread which reads messages to transmit from a queue.  Messages can
 * be added to queue with <code>#queueMessage(ProbeMessage)</code> method.
 * @author mike
 *
 */
class MessageSender extends Thread {
	
	/**
	 * Queue of messages to send.
	 */
	private final BlockingQueue<ProbeMessage> sendQueue;
	
	/**
	 * Next sequence number to use for sent messages.
	 */
	private int sequenceNumber;

	private final IChannelMessageSerializer serializer;
	/**
	 * Reference to the <code>Messenger</code> this <code>MessageSender</code> belongs to
	 */
	private final Messenger messenger;
	
	private final ILogger logger;
	/**
	 * Create a new message sender without starting it.
	 * @param channel Channel to send messages to.
	 */
	MessageSender(IChannelMessageSerializer serializer, Messenger messenger, ILogger logger) {
		setName("Probe Message Sending Thread");
		setDaemon(true);
		this.serializer = serializer;
		this.messenger = messenger;
		this.logger = logger;
		sendQueue = new LinkedBlockingQueue<ProbeMessage>();
	}
	
	/**
	 * Return sequence number to use in outgoing messages.
	 * @return The current sequence number.
	 */
	private synchronized int nextSequenceNumber() {
		if(sequenceNumber == 0) {
			sequenceNumber++;
		}
		return sequenceNumber++;
	}
	
	/**
	 * Queue message for transmission across channel.  If the message does not
	 * yet have a sequence number, a new one is chosen for this message.
	 * @param message <code>ProbeMessage</code> to send.
	 */
	void queueMessage(ProbeMessage message) {
		if(message.getSequenceNumber() == 0) {
			message.setSequenceNumber( nextSequenceNumber() );
		}
		
		sendQueue.add(message);
	}
	
	/**
	 * Message sending loop which reads messages from queue
	 * and sends them across channel.
	 */
	public void run() {
		logger.debug("Starting message sender");
		while(!interrupted()) {
			try {
				serializer.sendMessage( sendQueue.take() );
			} catch (InterruptedException e) {
				interrupt();
			} catch (ChannelException e) {
				interrupt();
			} catch (Exception e) {
				logger.error("Unexpected exception sending message", e);
				interrupt();
			}
		}
		
		sendQueue.clear();
		messenger.close();
		
	}

}
