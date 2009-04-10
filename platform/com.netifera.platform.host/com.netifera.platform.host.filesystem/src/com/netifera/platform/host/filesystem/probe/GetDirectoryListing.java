package com.netifera.platform.host.filesystem.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;
import com.netifera.platform.host.filesystem.File;

public class GetDirectoryListing extends ProbeMessage {
	
	 
	private static final long serialVersionUID = -9213737500680660072L;

	public static final String ID = "GetDirectoryListing";
	
	private final File[] entries;
	private final String path;
	
	GetDirectoryListing(String path) {
		super(ID);
		this.path = path;
		this.entries = null;
	}
	
	GetDirectoryListing createResponse(File[] entries) {
		return new GetDirectoryListing(entries, getSequenceNumber());
	}
	
	private GetDirectoryListing(File[] entries, int sequenceNumber) {
		super(ID);
		this.entries = entries;
		this.path = null;
		setSequenceNumber(sequenceNumber);
		markAsResponse();
	}
	
	public String getDirectoryPath() {
		return path;
	}
	
	public File[] getDirectoryEntries() {
		return entries;
	}
}
