package com.netifera.platform.net.packets.decoders;

import java.nio.ByteBuffer;

import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.net.packets.tcpip.ICMPv4;
import com.netifera.platform.net.packets.tcpip.IP;
import com.netifera.platform.net.packets.tcpip.IPv4;
import com.netifera.platform.net.packets.tcpip.TCP;
import com.netifera.platform.net.packets.tcpip.UDP;
import com.netifera.platform.util.NetworkConstants;

class IPv4Decoder extends AbstractIPDecoder  {

	private IPacketDecoder ipv6Decoder;
	
	IPv4Decoder() {}
	
	void setIPv6Decoder(IPv6Decoder decoder) {
		ipv6Decoder = decoder;
	}
	
	protected IP createIP() {
		return new IPv4();
	}
	
	protected IPacketHeader decodeNext(int protocol, ByteBuffer buffer) {
		IPacketHeader next = null;
		
		switch(protocol) {
		case NetworkConstants.IPPROTO_ICMP:
			next = new ICMPv4();
			break;
			
		case NetworkConstants.IPPROTO_IPIP: /* IPv4 over IPv4 tunnels */
			next = new IPv4();
			break;
			
		case NetworkConstants.IPPROTO_UDP:
			next = new UDP();
			break;
			
		case NetworkConstants.IPPROTO_TCP:
			next = new TCP();
			break;
			
		case NetworkConstants.IPPROTO_IPV6: /* IPv6 over IPv4 tunnels */
			return ipv6Decoder.decode(buffer);
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
