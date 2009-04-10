package com.netifera.platform.host.terminal.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class TerminalInput extends ProbeMessage {
	
	private static final long serialVersionUID = 7912093146978148020L;
	public static final String ID = "TerminalInput";
	
	private final String pty;
	private final byte[] input;
	
	TerminalInput(String pty, byte[] input) {
		super(ID);
		this.pty = pty;
		this.input = input;
	}
	
	String getPty() {
		return pty;
	}
	byte[] getInput() {
		return input;
	}
}
