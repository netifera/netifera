package com.netifera.platform.net.pcap;

import java.nio.ByteBuffer;

public interface IPacketHandler {
	/**
	 * Callback to process a single captured packet.  The buffer <code>packetData</code>
	 * contains the raw packet bytes.  The packet data includes all bytes between the 
	 * <tt>position</tt> and <tt>limit</tt> of the <code>ByteBuffer</code>.
	 * 
	 * @param packetData Buffer containing the packet data to process.
	 * @param header Structure containing metadata about captured packet.
	 */
	void handlePacket(ByteBuffer packetData, ICaptureHeader header);

}
