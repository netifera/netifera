package com.netifera.platform.host.terminal.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class TerminalOutput extends ProbeMessage {
	

	private static final long serialVersionUID = -8103923220782982906L;
	public static final String ID = "TerminalOutput";
	private final byte[] output;
	private final String pty;
	TerminalOutput(String pty, byte[] output) {
		super(ID);
		this.pty = pty;
		this.output = output;
	}
	
	String getPty() {
		return pty;
	}
	
	byte[] getOutput() {
		return output;
	}

}
