package com.netifera.platform.internal.dispatcher;

import com.netifera.platform.api.dispatcher.MessengerException;

public class MessengerClosedException extends MessengerException {

	private static final long serialVersionUID = 1L;

	public MessengerClosedException() {
		super("Messenger is closed.");
	}

}
