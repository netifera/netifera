package com.netifera.platform.net.daemon.sniffing;

import com.netifera.platform.net.packets.link.ARP;

/**
 * This interface should be implemented by sniffing daemon modules which wish to
 * receive ARP protocol packets from the network.
 * 
 * @see ISniffingModule
 * @see com.netifera.platform.net.packets.link.ARP
 */
public interface IArpSniffer extends ISniffingModule {
	/**
	 * This callback will deliver ARP packets which are received by the sniffing
	 * daemon.
	 * 
	 * @param arp  Received ARP packet.
	 * @param ctx Packet context information.
	 */
	void handleArpPacket(ARP arp, IPacketModuleContext ctx);
}
