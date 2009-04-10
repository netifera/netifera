package com.netifera.platform.net.packets.decoders;

import java.nio.ByteBuffer;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.net.packets.link.ARP;
import com.netifera.platform.util.NetworkConstants;

/**
 * A decoder implementation which can interpret various link types without fully parsing the 
 * link headers.  Only the information which is needed to find the higher level protocols is
 * examined and only the higher level protocols are fully parsed.
 * 
 * The following link layer types are currently supported:
 * 
 * <ul>
 *   <li> DLT_IEEE802 </li>
 *   <li> DLT_IEEE802_11 </li>
 *   <li> DLT_IEEE802_11_RADIO </li>
 *   <li> DLT_LINUX_SLL </li>
 *   <li> DLT_PRISM_HEADER </li>
 * </ul>
 * 
 */
public class GenericDecoder implements IPacketDecoder {

	private final static IPDecoder ipDecoder = new IPDecoder();
	
	// FIXME use pcap package? ; Datalink.DLT_IEEE802_11, ...
	private final static int DLT_IEEE802                = 6;
	private final static int DLT_IEEE802_11             = 105;
	private final static int DLT_LINUX_SLL				= 113;
	private final static int DLT_PRISM_HEADER			= 119;
	private final static int DLT_IEEE802_11_RADIO       = 127;
	
	private final static int DATA_FRAME                 = 2;
	private final static int FLAG_TO_DS		            = 0x01;
	private final static int FLAG_FROM_DS		        = 0x02;
	private final static int WIFI_DATA_MINSIZE          = 52;
	private final static int LLC_DATA_MINSIZE           = 28;
	private final static int LINUX_COOKED_DATA_MINSIZE  = 16;
	private final static int PRISM_DATA_MINSIZE			= 0x90;
	
	private final int datalink;
	
	public static GenericDecoder createForDatalink(int dlt) {
		switch(dlt) {
		case DLT_IEEE802:
		case DLT_IEEE802_11:
		case DLT_IEEE802_11_RADIO:
		case DLT_LINUX_SLL:
		case DLT_PRISM_HEADER:
			return new GenericDecoder(dlt);
		default:
			return null;
		}
	}
	private GenericDecoder(int  datalink) {
		this.datalink = datalink;
	}
	
	public IPacketHeader decode(ByteBuffer buffer) {
		switch(datalink) {
		case DLT_IEEE802:
		case DLT_IEEE802_11:
			return decodeWifi(buffer.slice());
			
		case DLT_IEEE802_11_RADIO:
			return decodeRadiotap(buffer.slice());
			
		case DLT_LINUX_SLL:
			return decodeLinuxCooked(buffer.slice());
			
		case DLT_PRISM_HEADER:
			return decodePrism(buffer.slice());
			
		default:
			throw new IllegalStateException("Illegal datalink in GenericDecoder");	
		}
	}
	
	private IPacketHeader decodeWifi(ByteBuffer buffer) {
		if(buffer.remaining() < WIFI_DATA_MINSIZE) {
			return new PacketPayload(buffer);
		}
		
		int x = buffer.get(0) & 0xff;
		int mainType = (x & 0x0c) >> 2;
		// If it's not a data frame just return it all in a payload
		if(mainType != DATA_FRAME) {
			return new PacketPayload(buffer);
		}
		
		x = buffer.get(1) & 0xff;
		int offset = 24;
		if( ((x & FLAG_FROM_DS) != 0) && ((x & FLAG_TO_DS) != 0) ) {
			offset += 6;
		}
		buffer.position( buffer.position() + offset);
		
		return decodeLLC(buffer.slice());
	}
	
	private IPacketHeader decodeRadiotap(ByteBuffer buffer) {
		// TODO check size
		buffer.getShort(); // skip version, pad
		int len = AbstractPacket.swap16(buffer.getShort() & 0xFFFF);
		if(len < 8 || len > buffer.remaining()) {
			buffer.rewind();
			return new PacketPayload(buffer);
		}
		buffer.position( buffer.position() + len );
		return decodeWifi(buffer.slice());
	}
	
	private IPacketHeader decodePrism(ByteBuffer buffer) {
		if(buffer.remaining() < PRISM_DATA_MINSIZE) {
			return new PacketPayload(buffer);
		}
		buffer.position( buffer.position() + PRISM_DATA_MINSIZE );
		
		return decodeWifi(buffer.slice());
	}
	
	private IPacketHeader decodeLinuxCooked(ByteBuffer buffer) {
		if(buffer.remaining() < LINUX_COOKED_DATA_MINSIZE) {
			return new PacketPayload(buffer);
		}
		buffer.position( buffer.position() + 14);

		return decodeProto(buffer, buffer.getShort() & 0xFFFF);
	}
	
	private IPacketHeader decodeLLC(ByteBuffer buffer) {
		if(buffer.remaining() < LLC_DATA_MINSIZE) {
			return new PacketPayload(buffer);
		}
		
		buffer.position( buffer.position() + 6);
		final int protocol = buffer.getShort() & 0xFFFF;
		
		return decodeProto(buffer, protocol);
	}
	
	private IPacketHeader decodeProto(ByteBuffer buffer, int protocol) {		
		switch(protocol) {
		case NetworkConstants.ETHERTYPE_ARP:
			final IPacketHeader arp = new ARP();
			if(arp.unpack(buffer.slice())) {
				return arp;
			}
			return new PacketPayload(buffer.slice());
			
		case NetworkConstants.ETHERTYPE_IPv4:
			return ipDecoder.getIPv4Decoder().decode(buffer.slice());
			
		case NetworkConstants.ETHERTYPE_IPv6:
			return ipDecoder.getIPv6Decoder().decode(buffer.slice());
			
		default:
			return new PacketPayload(buffer.slice());
		}
	}
}
