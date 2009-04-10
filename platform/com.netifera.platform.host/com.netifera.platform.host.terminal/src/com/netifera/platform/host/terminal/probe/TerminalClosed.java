package com.netifera.platform.host.terminal.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class TerminalClosed extends ProbeMessage {
	
	private static final long serialVersionUID = 8919753670859490593L;
	public final static String ID = "TerminalClosed";
	private final String ptyName;
	
	TerminalClosed(String ptyName) {
		super(ID);
		this.ptyName = ptyName;
	}
	
	String getPtyName() {
		return ptyName;
	}

}
