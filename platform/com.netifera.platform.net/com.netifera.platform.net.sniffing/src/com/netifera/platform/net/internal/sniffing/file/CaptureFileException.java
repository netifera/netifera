package com.netifera.platform.net.internal.sniffing.file;


public class CaptureFileException extends Exception {

	
	private static final long serialVersionUID = 1L;

	public CaptureFileException(String string) {
		super(string);
	}

	public CaptureFileException(String string, Throwable e) {
		super(string, e);
	}

}
