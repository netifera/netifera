package com.netifera.platform.net.internal.pcap;

import com.netifera.platform.net.pcap.Datalink;
import com.netifera.platform.net.pcap.IPacketHandler;


/**
 * This interface is implemented by providers of a native packet capture 
 * implementation.
 * 
 *
 */
public interface INativePacketCapture {
	boolean openLive(String device, int snaplen, int timeout, boolean promiscuous);
	boolean packetRead(IPacketHandler handler);
	boolean setDataLink(Datalink dlt);
	Datalink getLinkType();
	int getFileDescriptor();


	void close();

}
