package com.netifera.platform.net.sniffing;

import com.netifera.platform.net.pcap.ICaptureHeader;

/**
 * This interface provides access to information which is needed while
 * processing a captured packet.   It is passed as a parameter to the
 * callback methods of sniffing module implementations so that the
 * information is available during the handling and processing of
 * captured network information.
 * 
 * The information includes some data about the captured packet itself
 * as well as model related information (realms and spaces) which is required
 * when creating new entities.
 */
public interface IPacketContext {
	/**
	 * Return the capture header for this packet.
	 * 
	 * @return The capture header for this packet.
	 */
	ICaptureHeader getCaptureHeader();
	
	Object getPacketTag();
	void setPacketTag(Object tag);
	void abortProcessing();
	boolean isAborted();

}
