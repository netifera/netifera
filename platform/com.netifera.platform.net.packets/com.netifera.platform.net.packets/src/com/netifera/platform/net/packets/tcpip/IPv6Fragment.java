package com.netifera.platform.net.packets.tcpip;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.util.NetworkConstants;

public class IPv6Fragment extends AbstractPacket implements IPFragment, IPv6Encapsulable {
	private int nexthdr;			// 8-bit selector.
	private int reserved;			// 8-bit reserved field.
	private int frag_off = 0;		// 13-bit unsigned integer.
	//private int RF = 0;				// 2-bit reserved field.
	private int identification;		// 32 bits.
	
	/**
	 * The More-Fragments Flag carried in the header flags field.
	 * 
	 * false = Last Fragment,
	 * true = More Fragments.
	 */
	private boolean MF = false;
	
	public IPv6Fragment() {};
	
	public IPv6Fragment(IPacketHeader payload) {
		super(payload);
	}
	
	public IPv6 createPacket() {
		return new IPv6();
	}
	
	@Override
	protected int minimumHeaderLength() {
		return 8;
	}
	
	@Override
	protected void packHeader() {
		pack8(nexthdr);
		pack8(reserved);
		pack16(packFlagsFragment());
		pack32(identification);
	}
	
	@Override
	protected void unpackHeader() {
		nexthdr = unpack8();
		reserved = unpack8();
		unpackFlagsFragment(unpack16());
		identification = unpack32();
	}
	
	private int packFlagsFragment() {
		int n = frag_off / 8;
		
		// TODO RF
		
		if (MF) {
			n |= FL_MF;
		}
		
		return n;
	}
	
	private void unpackFlagsFragment(int value) {
		frag_off = (value & FRAGMENT_MASK) * 8;
		//RF = (value & FL_RESERVED);
		MF = ((value & FL_MF) != 0);
	}
	
	@Override
	public String toString() {
		StringBuffer out = new StringBuffer();
		out.append("IPv6 Fragment ");
		if (hasMoreFragments()) out.append(" (MF)");
		out.append(" offset="+getFragmentOffset()+" length="+getLength());
		return out.toString();
	}
	
	/**
	 * Identifies the initial header type of the Fragmentable Part of the
	 * original packet.
	 * Uses the same values as the IPv4 Protocol field
	 */
	@Override
	protected int nextProtocol() {
		return nexthdr;
	}
	
	@Override
	public PacketPayload payload() {
		return new PacketPayload(headerBufferSlice(getHeaderLength()));
	}
	
	public void setIdentification(int value) {
		identification = value;
	}
	
	public int getIdentification() {
		return identification;
	}
	
	public int protocolOverIPv6() {
		return NetworkConstants.IPPROTO_IPv6Fragment;
	}
	
	public int getFragmentOffset() {
		return frag_off;
	}
	
	public void setFragmentOffset(int value) {
		verifyMaximum(value, FRAGMENT_MASK);
		frag_off = value;
	}
	
	public boolean hasMoreFragments() {
		return MF;
	}
	
	public void setMoreFragments(boolean value) {
		MF = value;
	}
	
	public void setReserved(int value) {
		reserved = value;
	}
	
	public int getReserved() {
		return reserved;
	}
}
