package com.netifera.platform.api.dispatcher;

public interface IProbeMessage {

	/**
	 * Get sequence number for this message.
	 * @see #setSequenceNumber(int)
	 * @return Sequence number of this message.
	 */
	int getSequenceNumber();

	boolean isResponse();

	/**
	 * Returns the named type of this message.
	 */
	String getNamedType();

}