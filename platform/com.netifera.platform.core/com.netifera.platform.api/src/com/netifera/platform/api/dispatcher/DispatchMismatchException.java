package com.netifera.platform.api.dispatcher;

public class DispatchMismatchException extends DispatchException {
	
	private static final long serialVersionUID = 1L;

	public DispatchMismatchException(final IProbeMessage message) {
		super("Mismatched Message " + message.getNamedType());
	}

}
