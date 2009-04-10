package com.netifera.platform.net.daemon.sniffing;

import com.netifera.platform.net.packets.IPacketHeader;
/**
 * This interface should be implemented by sniffing daemon modules which wish to
 * receive raw link level protocol frames from the network.
 * 
 * @see ISniffingModule
 * @see com.netifera.platform.net.packets.IPacketHeader
 */
public interface IRawSniffer extends ISniffingModule {
	/**
	 * This callback will deliver raw packets which are received by the sniffing
	 * daemon.
	 * 
	 * @param raw  Received raw packet.
	 * @param ctx Packet context information.
	 */
	void handleRawPacket(IPacketHeader raw, IPacketModuleContext ctx);
}
