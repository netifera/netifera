package com.netifera.platform.net.wifi.pcap;

import com.netifera.platform.net.pcap.IPacketCapture;

/**
 * An extension of the packet capture interface for capturing on wireless devices.
 *  
 */
public interface IWifiPacketCapture extends IPacketCapture {
	/**
	 * Enables or disables monitor mode on the currently opened interface.
	 * 
	 * @param enable True for enable, false for disable.
	 * @return Returns false if the device is not opened or if setting monitor mode fails.
	 */
	boolean enableMonitorMode(boolean enable);
	
	
	/**
	 * Sets this wireless interface to the specified 802.11 channel.
	 * 
	 * @param channel The channel to set on this interface.
	 * @return True if the operation succeeds, false otherwise.
	 */
	boolean setChannel(int channel);
}
