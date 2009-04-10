package com.netifera.platform.host.terminal.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class OpenTerminal extends ProbeMessage {
	
	private static final long serialVersionUID = -2005921409308700285L;
	public final static String ID = "OpenTerminal";
	
	private final String command;
	private final String pty;
	
	OpenTerminal(String command) {
		super(ID);
		this.command = command;
		pty = null;
	}
	
	OpenTerminal createResponse(String pty) {
		return new OpenTerminal(pty, getSequenceNumber());
	}
	
	private OpenTerminal(String pty, int sequenceNumber) {
		super(ID);
		this.command = null;
		this.pty = pty;
		setSequenceNumber(sequenceNumber);
		markAsResponse();
	}
	
	String getCommand() {
		return command;
	}
	
	String getPty() {
		return pty;
	}

}
