package com.netifera.platform.net.internal.daemon.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class CancelCaptureFile extends ProbeMessage {
	
	private static final long serialVersionUID = 3525556017068724188L;
	public final static String ID = "CancelCaptureFile";

	public CancelCaptureFile() {
		super(ID);
	}
}
