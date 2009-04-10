package com.netifera.platform.net.internal.daemon.probe;

import com.netifera.platform.net.sniffing.ICaptureFileInterface;
import com.netifera.platform.net.sniffing.util.ICaptureFileProgress;

public class CaptureFileStub implements ICaptureFileInterface {

	private final String captureFilePath;
	private final boolean isValid;
	private final String errorMessage;
	
	CaptureFileStub(String path, boolean valid, String errorMessage) {
		this.captureFilePath = path;
		this.isValid = valid;
		this.errorMessage = errorMessage;
	}
	public String getErrorMessage() {
		return errorMessage;
		
	}

	public String getPath() {
		return captureFilePath;
	}

	public boolean isValid() {
		return isValid;
	}

	public void cancelProcessing() {
		
	}
	
	public void process() {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void process(ICaptureFileProgress progress) {
		process();		
	}

	public boolean captureAvailable() {
		return isValid;
	}

	public String getName() {
		return "Pcap Capture [" + captureFilePath + "]";
	}

	public void dispose() {
		
	}

}
