package com.netifera.platform.net.packets.tcpip;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.net.packets.link.EthernetEncapsulable;
import com.netifera.platform.util.NetworkConstants;
import com.netifera.platform.util.addresses.inet.IPv6Address;

public class IPv6 extends AbstractPacket implements IP, EthernetEncapsulable, IPv4Encapsulable, IPv6Encapsulable {
	private static final int IPv6_VERSION = 6;
	private int version = IPv6_VERSION;
	private int trafficClass = 0;
	private int flowLabel = 0;
	private int payloadLength;
	private int nextHeader;
	private int hopLimit = 255;
	private IPv6Address sourceAddress;
	private IPv6Address destinationAddress;

	/**

    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |Version| Traffic Class |           Flow Label                  |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |         Payload Length        |  Next Header  |   Hop Limit   |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                                                               |
   +                                                               +
   |                                                               |
   +                         Source Address                        +
   |                                                               |
   +                                                               +
   |                                                               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                                                               |
   +                                                               +
   |                                                               |
   +                      Destination Address                      +
   |                                                               |
   +                                                               +
   |                                                               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

   */
	
	public IPv6() {};
	
	public IPv6(IPacketHeader payload) {
		super(payload);
	}

	public IPv6(IPv6Encapsulable payload) {
		super(payload);
		nextHeader = payload.protocolOverIPv6();
	}
	
	public IPv6 createPacket() {
		return new IPv6();
	}
	
	@Override
	protected int minimumHeaderLength() {
		return 40;
	}

	@Override
	protected void packHeader() {
		pack32(flowLabel | (trafficClass << 20) | (version << 28));
		pack16(payloadLength);
		pack8(nextHeader);
		pack8(hopLimit);
		packAddress(sourceAddress);
		packAddress(destinationAddress);
	}

	@Override
	protected void unpackHeader() {
		int misc = unpack32();
		version = misc >> 28;
		trafficClass = (misc >> 20) & 0xFF;
		flowLabel = misc & 0xFFFFF;
		payloadLength = unpack16();
		nextHeader = unpack8();
		hopLimit = unpack8();
		sourceAddress = unpackAddress();
		destinationAddress = unpackAddress();
	}
	
	private IPv6Address unpackAddress() {
		return new IPv6Address(unpackBytes(16));
	}

	private void packAddress(IPv6Address address) {
		packBytes(address.toBytes());
	}

	@Override
	protected boolean isValidHeader() {
		return version == IPv6_VERSION;
	}
	
	@Override
	protected int nextProtocol() {
		return nextHeader;
	}
	
	/* can be used to forge invalid packets */
	public void setVersion(int value) {
		verifyMaximum(value, 0xF);
		version = value;
	}
	
	public int getVersion() {
		return version;
	}
	
	public int protocolOverEthernet() {
		return NetworkConstants.ETHERTYPE_IPV6;
	}
	
	public int protocolOverIPv4() {
		return NetworkConstants.IPPROTO_IPV6;
	}
	
	public int protocolOverIPv6() {
		return NetworkConstants.IPPROTO_IPV6; // FIXME verify
	}
	
	public int protocolOverPPP() {
		return NetworkConstants.PPP_IPV6;
	}
	
	@Override
	public String toString() {
		StringBuffer out = new StringBuffer();
		out.append("IPv6 ");
		if (isFragment()) out.append("Fragment ");
		out.append(sourceAddress);
		out.append(" -> ");
		out.append(destinationAddress);
		return out.toString();
	}
	
	/**
	 * Set the source address.
	 * 
	 * @param address The source address
	 */
	public void setSourceAddress(IPv6Address address) {
		sourceAddress = address;
	}
	
	public IPv6Address getSourceAddress() {
		return sourceAddress;
	}
	
	/**
	 * Set the destination address.
	 * 
	 * @param address The destination address
	 */
	public void setDestinationAddress(IPv6Address address) {
		destinationAddress = address;
	}
	
	public IPv6Address getDestinationAddress() {
		return destinationAddress;
	}

	/* Any extension headers present are considered part of the payload */
	@Override
	public PacketPayload payload() {
		return new PacketPayload(headerBufferSlice(getHeaderLength()));
	}
	
	@Deprecated // TODO verify the extensions and further IP payloads to sum
	public int getTotalLength() {
		return headerLength() + payloadLength;
	}
	
	@Deprecated // TODO verify the extensions
	public void setTotalLength(int length) {
		length -= headerLength();
		verifyMaximum(length, 0xFFFF);
		payloadLength = length;
	}
	
	public boolean isFragment() {
		return fragment() != null;
	}
	
	public IPFragment fragment() {
		return (IPFragment)findHeader(IPv6Fragment.class);
	}
}
