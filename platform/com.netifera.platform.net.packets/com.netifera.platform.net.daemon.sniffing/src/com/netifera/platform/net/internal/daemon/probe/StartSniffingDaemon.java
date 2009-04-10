package com.netifera.platform.net.internal.daemon.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class StartSniffingDaemon extends ProbeMessage {
	
	private static final long serialVersionUID = -4318230920526273341L;

	public final static String ID = "StartSniffingDaemon";
	
	private final long spaceId;
	
	public StartSniffingDaemon(long spaceId) {
		super(ID);
		this.spaceId = spaceId;
	}
	
	public long getSpaceId() {
		return spaceId;
	}


}
