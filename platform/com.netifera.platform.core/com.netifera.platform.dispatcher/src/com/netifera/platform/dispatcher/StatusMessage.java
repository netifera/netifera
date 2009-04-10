package com.netifera.platform.dispatcher;

import com.netifera.platform.api.dispatcher.ProbeMessage;


public class StatusMessage extends ProbeMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4780549436523334043L;
	
	private boolean error;
	private boolean unhandled;
	private String errorMessage;
	
	public static StatusMessage createOk(int sequenceNumber) {
		return new StatusMessage(sequenceNumber, false, null);
	}
	
	public static StatusMessage createError(int sequenceNumber, String message) {
		return new StatusMessage(sequenceNumber, true, message);
	}
	
	public static StatusMessage createUnhandledMessageError(int sequenceNumber) {
		StatusMessage msg = new StatusMessage(sequenceNumber, true, null);
		msg.unhandled = true;
		return msg;
	}
	
	private StatusMessage(int sequenceNumber, boolean error, String message) {
		super("status");
		this.error = error;
		errorMessage = message;
		markAsResponse();
		setSequenceNumber(sequenceNumber);
	}
	
	public boolean isOk() {
		return error == false;
	}
	
	public boolean isError() {
		return error;
	}
	
	public boolean isUnhandledMessageError() {
		return unhandled;
	}
	
	public String getErrorMessage() {
		if(errorMessage == null) {
			return "";
		}
		
		return errorMessage;
	}
	
	public String toString() {
		return super.toString() + ' ' + 
			(isOk() ? "OK" : "Error: " + getErrorMessage());
	}
}
