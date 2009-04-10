package com.netifera.platform.model;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class SetModelIdPrefix extends ProbeMessage {
	
	private static final long serialVersionUID = -460657379056131232L;
	public static final String ID = "SetModelIdPrefix";
	private final long prefix;

	public SetModelIdPrefix(long prefix) {
		super(ID);
		this.prefix = prefix;
	}
	public long getPrefix() {
		return prefix;
	}
}
