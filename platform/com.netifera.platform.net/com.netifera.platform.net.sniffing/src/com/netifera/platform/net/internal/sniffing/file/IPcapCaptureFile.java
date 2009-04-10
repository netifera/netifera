package com.netifera.platform.net.internal.sniffing.file;

import com.netifera.platform.net.pcap.Datalink;

public interface IPcapCaptureFile {
	
	/**
	 * Open the capture file for parsing.
	 * 
	 * @throws CaptureFileException Thrown if the file cannot be opened for any reason.
	 */
	void open() throws CaptureFileException;
	
	ICaptureFileRecord readRecord() throws CaptureFileException;
	
	int getSnaplen();
	
	String getPath();
	
	int getProgress();

	int getPacketCount();
	
	Datalink getLinkType();

}
