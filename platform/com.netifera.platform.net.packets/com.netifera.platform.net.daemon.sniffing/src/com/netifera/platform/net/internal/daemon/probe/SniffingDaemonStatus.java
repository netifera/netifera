package com.netifera.platform.net.internal.daemon.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class SniffingDaemonStatus extends ProbeMessage {

	private static final long serialVersionUID = -5885512219887463301L;

	public final static String ID = "SniffingDaemonStatus";

	final boolean isRunning;
	public SniffingDaemonStatus() {
		super(ID);
		isRunning = false;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	public SniffingDaemonStatus createResponse(boolean isRunning) {
		return new SniffingDaemonStatus(isRunning, getSequenceNumber());
	}
	private SniffingDaemonStatus(boolean isRunning, int sequenceNumber) {
		super(ID);
		this.isRunning = isRunning;
		setSequenceNumber(sequenceNumber);
		markAsResponse();
	}

}
