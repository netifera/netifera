package com.netifera.platform.net.packets.decoders;

import java.nio.ByteBuffer;

import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;

/**
 * Decode a raw tcp/ip packet with no DLT header
 * 
 * @see com.netifera.platform.net.pcap.Datalink.DLT_RAW
 */
public class RawIPDecoder implements IPacketDecoder {
	private final static IPDecoder ipDecoder = new IPDecoder();

	public IPacketHeader decode(ByteBuffer buffer) {
		int version = buffer.get() >> 4;
		buffer.position(buffer.position() - 1);
		switch(version) {
		case 4:
			return ipDecoder.getIPv4Decoder().decode(buffer.slice());
			
		case 6:
			return ipDecoder.getIPv6Decoder().decode(buffer.slice());
			
		default: // should not happen!
			// FIXME throw exception?
			return new PacketPayload(buffer.slice());
		}
	}
}
