package com.netifera.platform.host.terminal.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class TerminalSizeChange extends ProbeMessage {
	
	private static final long serialVersionUID = -1164772944622956087L;
	public static final String ID = "TerminalSizeChange";
	
	private final String ptyName;
	private final int width;
	private final int height;
	
	TerminalSizeChange(String ptyName, int width, int height) {
		super(ID);
		this.ptyName = ptyName;
		this.width = width;
		this.height = height;
	}
	
	String getPtyName() {
		return ptyName;
	}
	
	int getWidth() {
		return width;
	}
	
	int getHeight() {
		return height;
	}

}
