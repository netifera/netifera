package com.netifera.platform.net.internal.daemon.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class RunCaptureFile extends ProbeMessage {
	
	private static final long serialVersionUID = 2348674911581753962L;

	public final static String ID = "RunCaptureFile";

	private final long spaceId;
	private final String path;

	public RunCaptureFile(long spaceId, String path) {
		super(ID);
		this.spaceId = spaceId;
		this.path = path;
	}
	
	public long getSpaceId() {
		return spaceId;
	}
	public String getPath() {
		return path;
	}
	

}
