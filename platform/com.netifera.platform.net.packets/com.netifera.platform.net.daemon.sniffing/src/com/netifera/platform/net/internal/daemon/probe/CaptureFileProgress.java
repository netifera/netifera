package com.netifera.platform.net.internal.daemon.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class CaptureFileProgress extends ProbeMessage {
	
	private static final long serialVersionUID = 3187899411920457329L;

	public final static String ID = "CaptureFileProgress";
	
	private final boolean isFinished;
	private final boolean isError;
	private final String errorMessage;
	private final int percent;
	private int count;
	
	public static CaptureFileProgress createFinished() {
		return new CaptureFileProgress(true, false, 0, 0, null);
	}
	
	public static CaptureFileProgress createUpdate(int percent, int count) {
		return new CaptureFileProgress(false, false, percent, count, null);
	}
	
	public static CaptureFileProgress createError(String message) {
		return new CaptureFileProgress(false, true, 0, 0, message);
	}
	
	public boolean isFinished() {
		return isFinished;
	}
	
	public boolean isError() {
		return isError;
	}
	
	public boolean isUpdate() {
		return !isFinished && !isError;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public int getPercent() {
		return percent;
	}
	
	public int getCount() {
		return count;
	}
	
	
	private CaptureFileProgress(boolean isFinished, boolean isError, int percent, int count, String errorMessage) {
		super(ID);
		this.isFinished = isFinished;
		this.isError = isError;
		this.percent = percent;
		this.count = count;
		this.errorMessage = errorMessage;
	}


}
