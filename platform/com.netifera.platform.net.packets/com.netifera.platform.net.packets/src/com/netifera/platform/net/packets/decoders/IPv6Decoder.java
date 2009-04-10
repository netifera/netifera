package com.netifera.platform.net.packets.decoders;

import java.nio.ByteBuffer;

import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.net.packets.tcpip.ICMPv6;
import com.netifera.platform.net.packets.tcpip.IP;
import com.netifera.platform.net.packets.tcpip.IPv6;
import com.netifera.platform.net.packets.tcpip.TCP;
import com.netifera.platform.net.packets.tcpip.UDP;
import com.netifera.platform.util.NetworkConstants;

class IPv6Decoder extends AbstractIPDecoder  {

	private IPacketDecoder ipv4Decoder;

	/* package private constructor */
	IPv6Decoder() {}
	
	void setIPv4Decoder(IPv4Decoder decoder) {
		ipv4Decoder = decoder;
	}
	
	protected IP createIP() {
		return new IPv6();
	}
	
	protected IPacketHeader decodeNext(int protocol, ByteBuffer buffer) {
		 IPacketHeader next = null;
		
		switch(protocol) {
		case NetworkConstants.IPPROTO_IP: /* IPv4 over IPv6 tunnels */
			return ipv4Decoder.decode(buffer);
			
		case NetworkConstants.IPPROTO_IPV6: /* IPv6 over IPv6 tunnels */
			next = new IPv6();
			break;
			
		case NetworkConstants.IPPROTO_ICMPV6:
			next = new ICMPv6();
			break;
			
		case NetworkConstants.IPPROTO_UDP:
			next = new UDP();
			break;
			
		case NetworkConstants.IPPROTO_TCP:
			next = new TCP();
			break;
			
		default:
			break;
		}
		
		if(next == null || !next.unpack(buffer)) {
			return new PacketPayload(buffer);
		}

		// process encapsulated protocols
		if (next.getNextProtocol() != -1) {
			next.setNextPacket(decodeNext(next.getNextProtocol(), buffer.slice()));
		}
		
		return next;
	}
}
