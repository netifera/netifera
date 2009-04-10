package com.netifera.platform.host.filesystem.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;
import com.netifera.platform.host.filesystem.File;

public class GetRoots extends ProbeMessage {
	
	private static final long serialVersionUID = 3191360401313277253L;

	public static final String ID = "GetRoots";
	
	private final File[] roots;
	
	GetRoots() {
		super(ID);
		roots = null;
	}
	
	GetRoots createResponse(File[] roots) {
		return new GetRoots(roots, getSequenceNumber());
	}
	
	private GetRoots(File[] roots, int sequenceNumber) {
		super(ID);
		this.roots = roots;
		setSequenceNumber(sequenceNumber);
		markAsResponse();
	}
	
	File[] getFileRoots() {
		return roots;
	}

}
