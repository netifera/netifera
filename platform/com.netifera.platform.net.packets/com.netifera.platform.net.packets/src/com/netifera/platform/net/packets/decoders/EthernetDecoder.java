package com.netifera.platform.net.packets.decoders;

import java.nio.ByteBuffer;

import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.net.packets.link.ARP;
import com.netifera.platform.net.packets.link.Ethernet;
import com.netifera.platform.util.NetworkConstants;



public class EthernetDecoder implements IPacketDecoder  {
	private final static IPDecoder ipDecoder = new IPDecoder();
	
	public IPacketHeader decode(ByteBuffer buffer) {
		final Ethernet ethernet = new Ethernet();
		final ByteBuffer ethernetBuffer = buffer.slice();
		
		if(!ethernet.unpack(ethernetBuffer)) {
			return new PacketPayload(ethernetBuffer);
		}
		
		ethernet.setNextPacket(decodeNext(ethernet.getNextProtocol(), ethernetBuffer.slice()));
		
		return ethernet;
	}
	
	private IPacketHeader decodeNext(int protocol, ByteBuffer buffer) {
		
		switch(protocol) {
		
		case NetworkConstants.ETHERTYPE_ARP:
			final IPacketHeader arp = new ARP();
			if(arp.unpack(buffer)) {
				return arp;
			}
			return new PacketPayload(buffer);
			
		case NetworkConstants.ETHERTYPE_IPv4:
			return ipDecoder.getIPv4Decoder().decode(buffer);
			
		case NetworkConstants.ETHERTYPE_IPv6:
			return ipDecoder.getIPv6Decoder().decode(buffer);
			
		default:
			return new PacketPayload(buffer);
		}
	}
}
