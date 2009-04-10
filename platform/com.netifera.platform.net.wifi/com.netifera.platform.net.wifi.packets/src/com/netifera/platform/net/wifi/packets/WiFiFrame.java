package com.netifera.platform.net.wifi.packets;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.util.addresses.MACAddress;


abstract public class WiFiFrame extends AbstractPacket {
	int version;
	int type;
	int mainType;
	int subType;
	boolean toDS;
	boolean fromDS;
	boolean moreFragments;
	boolean isRetry;
	boolean powerManagementStatus;
	boolean hasMoreData;
	boolean isProtected;
	boolean isStrictlyOrdered;
	int duration;
	MACAddress address1;
	MACAddress address2;
	MACAddress address3;
	int fragmentNumber;
	int sequenceNumber;

	// frame control flags
	static final int FLAG_TO_DS		=0x01;
	static final int FLAG_FROM_DS		=0x02;
	static final int FLAG_MORE_FRAGMENTS	=0x04;
	static final int FLAG_RETRY		=0x08;
	static final int FLAG_POWER_MGT		=0x10;
	static final int FLAG_MORE_DATA		=0x20;
	static final int FLAG_WEP		=0x40;
	static final int FLAG_ORDER		=0x80;
	
	// frame main types
	public static final int MGT_FRAME		=0x00;
	public static final int CONTROL_FRAME		=0x01;
	public static final int DATA_FRAME		=0x02;

	// header sizes
	static final int DATA_SHORT_HDR_LEN	=24;
	static final int DATA_LONG_HDR_LEN	=30;
	static final int MGT_FRAME_HDR_LEN	=24;

	// frame types
	public static final int MGT_ASSOC_REQ			= 0x00;
	public static final int MGT_ASSOC_RESP			= 0x01;
	public static final int MGT_REASSOC_REQ			= 0x02;
	public static final int MGT_REASSOC_RESP		= 0x03;
	public static final int MGT_PROBE_REQ			= 0x04;
	public static final int MGT_PROBE_RESP			= 0x05;
	public static final int MGT_BEACON				= 0x08;
	public static final int MGT_ATIM				= 0x09;
	public static final int MGT_DISASS				= 0x0a;
	public static final int MGT_AUTHENTICATION		= 0x0b;
	public static final int MGT_DEAUTHENTICATION	= 0x0c;
	public static final int CTRL_PS_POLL			= 0x1a;
	public static final int CTRL_RTS				= 0x1b;
	public static final int CTRL_CTS				= 0x1c;
	public static final int CTRL_ACKNOWLEDGEMENT	= 0x1d;
	public static final int CTRL_CFP_END			= 0x1e;
	public static final int CTRL_CFP_ENDACK			= 0x1f;
	public static final int DATA					= 0x20;
	public static final int DATA_CF_ACK				= 0x21;
	public static final int DATA_CF_POLL			= 0x22;
	public static final int DATA_CF_ACK_POLL		= 0x23;
	public static final int DATA_NULL_FUNCTION		= 0x24;
	public static final int DATA_CF_ACK_NOD			= 0x25;
	public static final int DATA_CF_POLL_NOD		= 0x26;
	public static final int DATA_CF_ACK_POLL_NOD	= 0x27;
		
	@Override
	protected void packHeader() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void unpackHeader() {
		unpackFrameControlField();
		duration = unpack16();
		unpackAddresses();
		unpackSequenceControlField();
	}
	
	private void unpackFrameControlField() {
		int x = unpack8();
		version = x & 0x03;
		mainType = (x & 0x0c) >> 2;
		subType	= (x & 0xf0) >> 4;
		type = (mainType << 4) | subType;
		
		x = unpack8();
		toDS = (x & FLAG_TO_DS) == 1;
		fromDS = ((x & FLAG_FROM_DS) >> 1) == 1;
		moreFragments = ((x & FLAG_MORE_FRAGMENTS) >> 2) == 1;
		isRetry	= ((x & FLAG_RETRY) >> 3) == 1;
		powerManagementStatus = ((x & FLAG_POWER_MGT) >> 4) == 1;
		hasMoreData = ((x & FLAG_MORE_DATA) >> 5) == 1;
		isProtected = ((x & FLAG_WEP) >> 6) == 1;
		isStrictlyOrdered = ((x & FLAG_ORDER) >> 7) == 1;
	}
	
	protected void unpackAddresses() {
		address1 = new MACAddress(unpackBytes(6));
		address2 = new MACAddress(unpackBytes(6));
		address3 = new MACAddress(unpackBytes(6));
	}
	
	protected void unpackSequenceControlField() {
		int x = unpack16();
		fragmentNumber = x & 0x0f;
		sequenceNumber = x >> 4;
	}
	
	@Override
	protected int minimumHeaderLength() {
		return 24;
	}
	

	public int protocol() {
		return 0;
	}

	public int type() {
		return type;
	}
	
	@Override
	public String toString() {
		WiFiFramePrinter printer = new WiFiFramePrinter();
		printer.print(this);
		return printer.toString();
	}

	public MACAddress receiver() {
		return address1;
	}
	
	public MACAddress transmitter() {
		return address2;
	}
	
	public boolean isProtected() {
		return isProtected;
	}
	
	public boolean isToDistribution() {
		return toDS;
	}
	
	public boolean isFromDistribution() {
		return fromDS;
	}
}
