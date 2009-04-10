package com.netifera.platform.net.internal.daemon.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class SniffingModuleOutput extends ProbeMessage {
	
	private static final long serialVersionUID = 4130991850755485052L;

	public final static String ID = "SniffingModuleOutput";

	private final String message;
	public SniffingModuleOutput(String message) {
		super(ID);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
