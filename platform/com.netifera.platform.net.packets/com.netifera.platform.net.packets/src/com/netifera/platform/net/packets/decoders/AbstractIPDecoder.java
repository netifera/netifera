package com.netifera.platform.net.packets.decoders;

import java.nio.ByteBuffer;

import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.net.packets.tcpip.IP;

abstract public class AbstractIPDecoder implements IPacketDecoder {

	public IPacketHeader decode(ByteBuffer buffer) {
		final IP ip = createIP();
		final ByteBuffer ipBuffer = buffer.slice();
		
		if(!ip.unpack(ipBuffer)) {
			return new PacketPayload(ipBuffer);
		}
		
		final int ipPayloadLength = ip.getTotalLength() - ip.getHeaderLength();
		
		if(ipPayloadLength < 0) {
			throw new IllegalStateException("isValidHeader() is broken for " + ip.getClass().getCanonicalName());
		}
		
		/* If packet is longer than IP header expects, then just trim it to the right length */

		if(ipPayloadLength < ipBuffer.remaining()) {
			final int trimCount = ipBuffer.remaining() - ipPayloadLength;
			ipBuffer.limit(ipBuffer.limit() - trimCount);
		}
		
		ip.setNextPacket(decodeNext(ip.getNextProtocol(), ipBuffer.slice()));
		return ip;
	}
	
	protected abstract IP createIP();
	protected abstract IPacketHeader decodeNext(int protocol, ByteBuffer buffer);

}
