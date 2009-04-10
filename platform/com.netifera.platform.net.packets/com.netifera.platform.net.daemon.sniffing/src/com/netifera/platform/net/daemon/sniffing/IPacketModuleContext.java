package com.netifera.platform.net.daemon.sniffing;

import com.netifera.platform.net.pcap.ICaptureHeader;

/**
 * This is an extension of the <code>IPacketContext</code> interface which adds
 * a method to allow sniffing daemon modules to print output to the console in
 * the UI.
 */
public interface IPacketModuleContext extends IModuleContext {
	
	/**
	 * Return the capture header for this packet.
	 * 
	 * @return The capture header for this packet.
	 */
	ICaptureHeader getCaptureHeader();
	
	/**
	 * Changes the current realm for processing this packet.  While processing a packet,
	 * some tools and sniffing modules may discover information that places this packet
	 * into a different realm.  An example of when this might happen is while processing
	 * raw wireless packets captured in monitor mode.  Modules that process the link layer
	 * can identify that data frames belong to a specific wireless network and adjust the 
	 * realm so that higher layers create entities inside the realm for the wireless network.
	 * 
	 * @param realm The new realm id.
	 */
	void setRealm(long realm);
	
	void abortProcessing();


}
