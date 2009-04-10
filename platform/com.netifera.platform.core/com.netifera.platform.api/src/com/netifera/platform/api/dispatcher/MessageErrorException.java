package com.netifera.platform.api.dispatcher;

public class MessageErrorException extends MessengerException {

	private static final long serialVersionUID = 1L;

	public MessageErrorException(final String message) {
		super(message);
	}

}
