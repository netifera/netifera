package com.netifera.platform.net.daemon.sniffing;

import com.netifera.platform.net.packets.tcpip.IPv4;
import com.netifera.platform.net.packets.tcpip.IPv6;

/**
 * This interface should be implemented by sniffing daemon modules which wish to
 * receive IPv4 and IPv6 packets from the network.
 *  
 * @see ISniffingModule
 * @see com.netifera.platform.net.packets.tcpip.IPv4
 * @see com.netifera.platform.net.packets.tcpip.IPv6
 *
 */
public interface IIPSniffer extends ISniffingModule {
	/**
	 * This callback will deliver IPv4 packets which are received by the
	 * sniffing daemon.
	 * 
	 * @param ipv4  Received IPv4 packet.
	 * @param ctx Packet context information.
	 */
	void handleIPv4Packet(IPv4 ipv4, IPacketModuleContext ctx);

	/**
	 * This callback will deliver IPv6 packets which are received by the
	 * sniffing daemon.
	 * 
	 * @param ipv6  Received IPv6 packet.
	 * @param ctx Packet context information.
	 */
	void handleIPv6Packet(IPv6 ipv6, IPacketModuleContext ctx);
}
