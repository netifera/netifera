package com.netifera.platform.net.packets.tcpip;

import java.nio.ByteBuffer;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketException;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.util.NetworkConstants;

public class UDP extends AbstractPacket implements IPseudoHeaderClient, IPv4Encapsulable, IPv6Encapsulable {
	private int sourcePort;
	private int destinationPort;
	private int length;
	private int checksum;
	
	/**
    0      7 8     15 16    23 24    31
    +--------+--------+--------+--------+
    |     Source      |   Destination   |
    |      Port       |      Port       |
    +--------+--------+--------+--------+
    |                 |                 |
    |     Length      |    Checksum     |
    +--------+--------+--------+--------+
    |
    |          data octets ...
    +---------------- ...
         User Datagram Header Format   
	 */
	@Override
	protected void packHeader() {	
		pack16(sourcePort);
		pack16(destinationPort);
		pack16(length);
		pack16(checksum);
	}
	
	@Override
	protected void unpackHeader() {
		sourcePort			= unpack16();
		destinationPort		= unpack16();
		length				= unpack16();
		checksum			= unpack16();
	}

	@Override
	protected boolean hasPayload() {
		return true;
	}

    private boolean lengthSet;
    private boolean checksumSet;
    private ByteBuffer pseudoHeader;

    public UDP() {}
    public UDP(IPacketHeader payload) {
    	super(payload);
    }    
    	
	public int protocolOverIPv4() {
		return NetworkConstants.IPPROTO_UDP;
	}

	public int protocolOverIPv6() {
		return NetworkConstants.IPPROTO_UDP;
	}

	@Override
	public int minimumHeaderLength() {
		return 8;
	}

	public int getSourcePort() {
		return sourcePort;
	}
	
	public void setSourcePort(int value) {
		verifyMaximum(value, 0xFFFF);
		sourcePort = value;
	}
	
	public int getDestinationPort() {
		return sourcePort;
	}
	
	public void setDestinationPort(int value) {
		verifyMaximum(value, 0xFFFF);
		destinationPort = value;
	}

	public void setLength(int value) {
		verifyMaximum(value, 0xFFFF);
		length = value;
		lengthSet = true;
	}
	
	public void setChecksum(int value) {
		verifyMaximum(value, 0xFFFF);
		checksum = value;
		checksumSet = true;
	}
	
	public void setPseudoHeader(ByteBuffer pseudoHeader) {
		this.pseudoHeader = pseudoHeader;
	}
	
	@Override
	protected void populateGeneratedFields() {
		if(!lengthSet) {
			length = getLength();
		}
		
		if(!checksumSet) {
			checksum = 0;
		}
	}

	@Override
	protected void calculateChecksum() {
		if(checksumSet) return;
		
		if(pseudoHeader == null) {
			throw new PacketException("Cannot calculate UDP checksum because parent has not sent pseudoheader");
		}
		
		int sum = generateChecksumWithPseudo(length, pseudoHeader);
		pack16(sum, 6);
	}
	
	@Override
	public String toString() {
		return "UDP " + sourcePort + " -> " + destinationPort;
	}

	@Override
	public PacketPayload payload() {
		return getNextHeader() == null ? PacketPayload.EMPTY_PAYLOAD : (PacketPayload) getNextHeader();
	}
}
