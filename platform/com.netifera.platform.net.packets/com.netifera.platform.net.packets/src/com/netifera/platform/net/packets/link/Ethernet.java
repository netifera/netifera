package com.netifera.platform.net.packets.link;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.util.addresses.MACAddress;


public class Ethernet extends AbstractPacket {

	private MACAddress destinationAddress;
	private MACAddress sourceAddress;
	private int type;
	/**
	 *   0   1   2   3   4   5   6   7   8   9   A   B   C   D
	 * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+
	 * | destination address   |    source address     | type  |
	 * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+
	 */
	@Override
	protected void packHeader() {
		packBytes(destinationAddress.toBytes());
		packBytes(sourceAddress.toBytes());
		pack16(type);		
	}

	@Override
	protected void unpackHeader() {
		destinationAddress		= new MACAddress(unpackBytes(6));
		sourceAddress			= new MACAddress(unpackBytes(6));
		type 					= unpack16();
	}
	
	private static final MACAddress nullAddress
				= new MACAddress(new byte[] { 0, 0, 0, 0, 0, 0 });
	
	public Ethernet() {}
	
	public Ethernet(EthernetEncapsulable payload) {
		super(payload);
		sourceAddress = nullAddress;
		destinationAddress = nullAddress;
		type = payload.protocolOverEthernet();
	}
	
	
	@Override
	public int nextProtocol() {
		return type;
	}
	
	@Override
	public int minimumHeaderLength() {
		return 14; /* 2 * sizeof(MACAddress) + 2 */
	}

	@Override
	public String toString() {
		return "Ethernet " + sourceAddress + " -> " + destinationAddress;
	}
}
