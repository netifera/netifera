package com.netifera.platform.host.terminal.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class CloseTerminal extends ProbeMessage {

	private static final long serialVersionUID = 7273049389784749215L;
	public final static String ID = "CloseTerminal";
	private final String ptyName;
	CloseTerminal(String ptyName) {
		super(ID);
		this.ptyName = ptyName;
	}
	
	String getPtyName() {
		return ptyName;
	}
}
