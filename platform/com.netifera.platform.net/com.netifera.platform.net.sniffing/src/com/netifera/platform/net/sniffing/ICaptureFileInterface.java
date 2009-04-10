package com.netifera.platform.net.sniffing;

import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.util.ICaptureFileProgress;


public interface ICaptureFileInterface extends ICaptureInterface {
	String getPath();
	void process();
	void dispose();
	void process(final ICaptureFileProgress progress);
	void cancelProcessing();
	boolean isValid();
	String getErrorMessage();
}
