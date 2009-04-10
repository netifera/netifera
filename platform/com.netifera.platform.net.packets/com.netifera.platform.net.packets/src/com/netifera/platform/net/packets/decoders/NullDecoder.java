package com.netifera.platform.net.packets.decoders;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;

public class NullDecoder implements IPacketDecoder {
	/*
	 * BSD AF_ values.
	 *
	 * Unfortunately, the BSDs don't all use the same value for AF_INET6,
	 * so, because we want to be able to read captures from all of the BSDs,
	 * we check for all of them.
	 */

	private final static int BSD_AF_INET = 2;
	private final static int BSD_AF_INET6_BSD = 24;
	private final static int BSD_AF_INET6_FREEBSD = 28;
	private final static int BSD_AF_INET6_DARWIN = 30;

	private final static IPDecoder ipDecoder = new IPDecoder();

	private final boolean nativeBigEndian = (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);

	/*
	 * The DLT_NULL packet header is 4 bytes long. It contains a host-byte-order
	 * 32-bit integer that specifies the family, e.g. AF_INET.
	 *
	 * Note here that "host" refers to the host on which the packets were
	 * captured; that isn't necessarily *this* host.
	 *
	 * The OpenBSD DLT_LOOP packet header is the same, except that the integer
	 * is in network byte order.
	 */
	public IPacketHeader decode(ByteBuffer buffer) {
		int proto = buffer.getInt();
		if(!nativeBigEndian) {
			proto = AbstractPacket.swap32(proto);
		}
		switch(proto) {
		case BSD_AF_INET:
			return ipDecoder.getIPv4Decoder().decode(buffer.slice());
		case BSD_AF_INET6_BSD:
		case BSD_AF_INET6_DARWIN:
		case BSD_AF_INET6_FREEBSD:
			return ipDecoder.getIPv6Decoder().decode(buffer.slice());
		default:
			return new PacketPayload(buffer.slice());
		}
	}

}
