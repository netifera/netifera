package com.netifera.platform.net.packets.tcpip;

import com.netifera.platform.net.packets.IPacketHeader;

public interface IPFragment extends IPacketHeader {
	
	int FL_RESERVED 		= (1 << 15);
	int FL_MF 				= (1 << 13);
	int FRAGMENT_MASK 		= 0x1FFF;

	/**
	 * Create a new IP packet of the same IP family of the current fragment.
	 */
	IP createPacket();
	
	/**
	 * An identifying value assigned by the sender to aid in assembling the
	 * fragments of a datagram.
	 */
	int getIdentification();
	
	void setIdentification(int value);
	
	/**
	 * indicate whether or not this datagram contains the end of an datagram,
	 * carried in the internet header Flags field.
	 */
	boolean hasMoreFragments();
	
	void setMoreFragments(boolean value);
	
	/**
	 * This field indicates where in the datagram this fragment belongs.
	 * The first fragment has offset zero.
	 */
	int getFragmentOffset();
	
	void setFragmentOffset(int value);
}
