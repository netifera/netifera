package com.netifera.platform.api.channels;

/**
 * Thrown to indicate a condition on a <code>Channel</code> which has caused
 * the channel to be closed.
 */
public class ChannelException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Construct a new empty <code>ChannelException</code>.
	 */
	public ChannelException() {
		super();
	}
	
	/**
	 * Construct a new <code>ChannelException</code> with a message which
	 * describes the error.
	 * @param message A description of the exception condition.
	 */
	public ChannelException(final String message) {
		super(message);
	}
	
	public ChannelException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
