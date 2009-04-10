package com.netifera.platform.net.packets.decoders;

import java.nio.ByteBuffer;

import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.net.packets.link.ARP;
import com.netifera.platform.net.packets.link.LLC;
import com.netifera.platform.util.NetworkConstants;


public class LLCDecoder implements IPacketDecoder {

	private final static IPDecoder ipDecoder = new IPDecoder();
	
	public IPacketHeader decode(ByteBuffer buffer) {
		final LLC llc = new LLC();
		final ByteBuffer llcBuffer = buffer.slice();
		
		if(!llc.unpack(llcBuffer)) {
			return new PacketPayload(llcBuffer);
		}
		
		llc.setNextPacket(decodeNext(llc.getNextProtocol(), llcBuffer.slice()));
		return llc;
	}
	
	private IPacketHeader decodeNext(int protocol, ByteBuffer buffer) {
		switch(protocol) {
		case NetworkConstants.ETHERTYPE_ARP:
			final IPacketHeader arp = new ARP();
			if(arp.unpack(buffer)) {
				return arp;
			} else {
				return new PacketPayload(buffer);
			}
		case NetworkConstants.ETHERTYPE_IPv4:
			return ipDecoder.getIPv4Decoder().decode(buffer);
			
		case NetworkConstants.ETHERTYPE_IPv6:
			return ipDecoder.getIPv6Decoder().decode(buffer);
			
		default:
			return new PacketPayload(buffer);
				
		}
	}
//	private static final LLCDecoder instance;
//	static {
//		instance = new LLCDecoder();
//		instance.put(LLC.class, ETHERTYPE_ARP, ARP.class);
//		instance.put(LLC.class, ETHERTYPE_IPv4, IPv4Decoder.defaultInstance());
//		instance.put(LLC.class, ETHERTYPE_IPv6, IPv6Decoder.defaultInstance());
//	}
//	
//	static public LLCDecoder defaultInstance() {return instance;}
//	
//	public LLCDecoder() {
//		super();
//	}
//
//	public IPacket decode(byte[] data, int offset, int length) {
//		return decode(data, offset, length, LLC.class);
//	}
}

