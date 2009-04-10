package com.netifera.platform.net.sniffing.util;

public interface ICaptureFileProgress {
	boolean updateProgress(int percent, int count);
	void error(String message, Throwable e);
	void finished();
}
