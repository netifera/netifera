package com.netifera.platform.net.packets.decoders;

import com.netifera.platform.net.packets.IPacketDecoder;

public class IPDecoder {
	
	final private IPv4Decoder ipv4Decoder;
	final private IPv6Decoder ipv6Decoder;
	
	public IPDecoder() {
		/* See ticket #178 if you're wondering why this class exists */
		ipv4Decoder = new IPv4Decoder();
		ipv6Decoder = new IPv6Decoder();
		ipv4Decoder.setIPv6Decoder(ipv6Decoder);
		ipv6Decoder.setIPv4Decoder(ipv4Decoder);
	}
	
	public IPacketDecoder getIPv4Decoder() {
		return ipv4Decoder;
	}
	
	public IPacketDecoder getIPv6Decoder() {
		return ipv6Decoder;
	}

}
