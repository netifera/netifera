package com.netifera.platform.net.internal.daemon.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class CaptureFileValid extends ProbeMessage {
	
	private static final long serialVersionUID = 1539335353344406255L;

	public final static String ID = "CaptureFileValid";
	
	private final String path;
	private final boolean valid;
	private final String errorMessage;
	
	public CaptureFileValid(String path) {
		super(ID);
		this.path = path;
		valid = false;
		errorMessage = null;
	}
	
	public CaptureFileValid createResponse(boolean isValid, String errorMessage) {
		return new CaptureFileValid(isValid, errorMessage, getSequenceNumber());
	}
	
	public String getPath() {
		return path;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	private CaptureFileValid(boolean valid, String errorMessage, int sequenceNumber) {
		super(ID);
		this.valid = valid;
		this.errorMessage = errorMessage;
		this.path = null;
		setSequenceNumber(sequenceNumber);
		markAsResponse();
	}

}
