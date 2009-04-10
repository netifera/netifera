package com.netifera.platform.model;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class RequestModelUpdate extends ProbeMessage {
	
	private static final long serialVersionUID = -5047162662178263748L;

	public final static String ID = "RequestModelUpdate";
	
	private final long startingIndex;
	
	public RequestModelUpdate(long updateIndex) {
		super(ID);
		this.startingIndex = updateIndex;
	}
	
	public long getStartingIndex() {
		return startingIndex;
	}

}
