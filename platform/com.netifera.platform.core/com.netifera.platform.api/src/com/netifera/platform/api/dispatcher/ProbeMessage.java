package com.netifera.platform.api.dispatcher;
import java.io.Serializable;

/**
 * Base class for all messages exchanged with probes.
 */
public class ProbeMessage implements Serializable, IProbeMessage {
	private static final long serialVersionUID = -1746870501140453759L;
	
	/**
	 * The sequence number for this message.  Used to match responses with
	 * initial messages.  
	 */
	private int sequenceNumber;
	
	/**
	 * This flag is set in response messages.
	 */
	private boolean sequenceNumberSet;
	
	/**
	 * This message is a response message
	 */
	private boolean isResponseBool;
	
	/**
	 * Named type of message for dispatching. 
	 */
	private final String namedType;
	
	/**
	 * Create new ProbeMessage with the specified name.
	 * @param namedType Named type of message for dispatching.
	 */
	protected ProbeMessage(final String namedType) {
		this.namedType = namedType;		
	}
	
	/**
	 * Sets the sequence number for this message.  Sequence numbers are used
	 * to match response messages to the initial message which generated them.
	 * @param value Sequence number value.
	 */
	public void setSequenceNumber(final int value) {
		sequenceNumber = value;
		sequenceNumberSet = true;
	}
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	
	/**
	 * Has a sequence number been set yet for this message?  The Messenger
	 * uses this method to know if it needs to generate and add a sequence
	 * number before transmitting the message.
	 */
	public boolean isSequenceNumberSet() {
		return sequenceNumberSet;
	}
	
	public boolean isResponse() {
		return isResponseBool;
	}
	
	protected void markAsResponse() {
		isResponseBool = true;
	}

	public void markAsNotResponse() {
		isResponseBool = false;
	}
	
	public String getNamedType() {
		return namedType;
	}
	
	@Override
	public String toString() {
		return getNamedType();
	}
	
	/**
	 * Convenience method for creating a 'Ok' {@link StatusMessage} response
	 * from this message.
	 * @return The response message
	 * @see StatusMessage
	 */
//	public StatusMessage createResponseOk() {
//		return StatusMessage.createOk(sequenceNumber);
//	}
	
	/**
	 * Convenience method for creating an error {@link StatusMessage} response
	 * from this message
	 * @param errorMessage Error string to set in response message.
	 * @return The response message.
	 * @see StatusMessage
	 */
//	public StatusMessage createResponseError(String errorMessage) {
//		return StatusMessage.createError(sequenceNumber, errorMessage);
//	}

}
