package com.netifera.platform.net.packets.tcpip;

import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.util.addresses.inet.InternetAddress;

/**
 * @see com.netifera.platform.net.packets.tcpip.IPv4
 * @see com.netifera.platform.net.packets.tcpip.IPv6
 */
public interface IP extends IPacketHeader {
	
	/* can be used to forge invalid packets */
	void setVersion(int value);
	
	int getVersion();
	
	/**
	 * Total Length is the length of the datagram, measured in octets, including
	 * header and data.
	 */
	int getTotalLength();
	
	void setTotalLength(int length);
	
	/**
	 * Get the source address.
	 * 
	 * @return The source address
	 */
	InternetAddress getSourceAddress();
	
	/**
	 * Get the destination address.
	 * 
	 * @return The destination address
	 */
	InternetAddress getDestinationAddress();
	
	boolean isFragment();

	IPFragment fragment();
}
