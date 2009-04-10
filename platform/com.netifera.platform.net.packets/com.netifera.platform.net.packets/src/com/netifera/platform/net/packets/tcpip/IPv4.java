package com.netifera.platform.net.packets.tcpip;

import java.nio.ByteBuffer;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.net.packets.PacketException;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.net.packets.link.EthernetEncapsulable;
import com.netifera.platform.util.NetworkConstants;
import com.netifera.platform.util.addresses.inet.IPv4Address;

public class IPv4 extends AbstractPacket implements IP, IPFragment, EthernetEncapsulable, IPv4Encapsulable, IPv6Encapsulable {
	private static final int IPv4_VERSION = 4;
	private int version = IPv4_VERSION;
	private int headerLength32;
	private int tos;
	private int totalLength;
	private int identification;
	
	private boolean reserved;
	private boolean CE;
	/**
	 * The Don't Fragment bit carried in the header flags field.
	 * 
	 * false: May Fragment,
	 * true: Don't Fragment.
	 */
	private boolean DF;
	/**
	 * The More-Fragments Flag carried in the header flags field.
	 * 
	 * false = Last Fragment,
	 * true = More Fragments.
	 */
	private boolean MF = false;
	
	private int fragmentOffset = 0;
	private int timeToLive;
	private int protocol;
	private int headerChecksum;

	private IPv4Address sourceAddress;
	private IPv4Address destinationAddress;
	
	/**
   
    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |Version|  IHL  |Type of Service|          Total Length         |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |         Identification        |Flags|      Fragment Offset    |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |  Time to Live |    Protocol   |         Header Checksum       |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                       Source Address                          |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                    Destination Address                        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                    Options                    |    Padding    |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   
   */
	
	@Override
	protected void packHeader() {
		pack8((version << 4) | headerLength32);
		pack8(tos);
		pack16(totalLength);
		pack16(identification);
		pack16(packFlagsFragment());
		pack8(timeToLive);
		pack8(protocol);
		pack16(headerChecksum);
		pack32(sourceAddress.toInteger());
		pack32(destinationAddress.toInteger());
	}

	@Override
	protected void unpackHeader() {
		int vhl = unpack8();
		version = (vhl >> 4) & 0x0F;
		headerLength32 = vhl & 0x0F;
		tos = unpack8();
		totalLength = unpack16();
		totalLengthSet = true;
		identification = unpack16();
		unpackFlagsFragment(unpack16());
		timeToLive = unpack8();
		protocol = unpack8();
		headerChecksum = unpack16();
		sourceAddress = new IPv4Address(unpack32());
		destinationAddress = new IPv4Address(unpack32());
	}

	@Override
	protected boolean isValidHeader() {
		return (version == IPv4_VERSION) && (headerLength32 >= 5) && (totalLength >= (headerLength32 * 4));
	}
	
	/** user override flags */
	private boolean identificationSet;
	private boolean checksumSet;
	private boolean totalLengthSet;
	
	public static final int FL_RESERVED 		= (1 << 15);
	public static final int FL_CE 				= FL_RESERVED;
	public static final int FL_DF 				= (1 << 14);
	
	public IPv4() {};
	public IPv4(IPv4Encapsulable payload) {
		super(payload);

		headerLength32 = 5;
		timeToLive = 255;
		DF = true;
		protocol = payload.protocolOverIPv4();
	}
	
	public IPv4 createPacket() {
		return new IPv4();
	}
	
	public int protocolOverEthernet() {
		return NetworkConstants.ETHERTYPE_IPv4;
	}
	
	public int protocolOverIPv4() {
		return NetworkConstants.IPPROTO_IPIP;
	}
	
	public int protocolOverIPv6() {
		return NetworkConstants.IPPROTO_IPV6; // FIXME verify
	}
	
	public int protocolOverPPP() {
		return NetworkConstants.PPP_IP;
	}
	
	@Override
	protected int nextProtocol() {
		/* value from header, but is it set yet? */
		return protocol;
	}
	
	public void setVersion(int value) {
		verifyMaximum(value, 0xF);
		version = value;
	}
	
	public int getVersion() {
		return version;
	}
	
	public void setHeaderLength32(int value) {
		verifyMaximum(value, 0xF);
		headerLength32 = value;
	}
	
	public int getHeaderLength32() {
		return headerLength32;
	}
	
	@Override
	public int headerLength() {
		return headerLength32 * 4;
	}
	
	@Override
	public int getLength() {
		if (totalLengthSet) {
			return getTotalLength();
		} else {
			return super.getLength();
		}
	}
	
	public void setTypeOfService(int value) {
		verifyMaximum(value, 0xFF);
		tos = value;
	}
	
	public int getTypeOfService() {
		return tos;
	}
	
	public void setTotalLength(int value) {
		verifyMaximum(value, 0xFFFF);
		totalLength = value;
		totalLengthSet = true;
	}
	
	public int getTotalLength() {
		return totalLength;
	}
	
	public void setIdentification(int value) {
		verifyMaximum(value, 0xFFFF);
		identification = value;
		identificationSet = true;
	}
	
	public int getIdentification() {
		return identification;
	}
	
	public void setReserved(boolean value) {
		reserved = value;
	}
	
	public boolean getReserved() {
		return reserved;
	}
	
	public void setCongestion(boolean value) {
		CE = value;
	}
	
	public boolean getCongestion() {
		return CE;
	}
	
	public void setDF(boolean value) {
		DF = value;
	}
	
	public boolean getDF() {
		return DF;
	}
	
	public void setMF(boolean value) {
		MF = value;
	}
	
	public boolean getMF() {
		return MF;
	}
	
	public boolean hasMoreFragments() {
		return getMF();
	}
	
	public void setMoreFragments(boolean value) {
		setMF(value);
	}
	
	public void setFragmentOffset(int value) {
		verifyMaximum(value, FRAGMENT_MASK);
		fragmentOffset = value;
	}
	
	public int getFragmentOffset() {
		return fragmentOffset;
	}
	
	public boolean isFragment() {
		return getMF() || getFragmentOffset() > 0;
	}
	
	public IPFragment fragment() {
		return this;
	}
	
	public void setTimeToLive(int value) {
		verifyMaximum(value, 0xFF);
		timeToLive = value;
	}
	
	public int getTimeToLive() {
		return timeToLive;
	}
	
	public void setProtocol(int value) {
		verifyMaximum(value, 0xFF);
		protocol = value;
	}
	
	public int getProtocol() {
		return protocol;
	}
	
	public void setHeaderChecksum(int value) {
		verifyMaximum(value, 0xFFFF);
		headerChecksum = value;
		checksumSet = true;
	}
	
	public int getHeaderChecksum() {
		return headerChecksum;
	}
	
	/**
	 * Set the source address.
	 * 
	 * @param address The source address
	 */
	public void setSourceAddress(IPv4Address address) {
		sourceAddress = address;
	}
	
	public IPv4Address getSourceAddress() {
		return sourceAddress;
	}
	
	/**
	 * Set the destination address.
	 * 
	 * @param address The destination address
	 */
	public void setDestinationAddress(IPv4Address address) {
		destinationAddress = address;
	}
	
	public IPv4Address getDestinationAddress() {
		return destinationAddress;
	}
	
	@Override
	protected void populateGeneratedFields() {
		if(getLength() > 0xFFFF) {
			throw new PacketException("Packet is too big to fit in IPv4 header");
		}
		
		if(!totalLengthSet) {
			totalLength = getLength();
		}
		
		if(!identificationSet) {
			generateIdentification();
		}
				
		if(!checksumSet) {
			headerChecksum = 0;
		}
		
		if(getNextHeader() instanceof IPseudoHeaderClient) {
			sendPseudoheaderInfo((IPseudoHeaderClient)getNextHeader());
		}
		
	}
	
	@Override
	protected void calculateChecksum() {
		if(!checksumSet) {
			headerChecksum = generateChecksum(headerLength32 * 4);
			pack16(headerChecksum, 10);
		}
	}
	
	private void sendPseudoheaderInfo(IPseudoHeaderClient target) {
		ByteBuffer pseudo = ByteBuffer.allocate(12);
		
		pseudo.putInt(sourceAddress.toInteger());
		pseudo.putInt(destinationAddress.toInteger());
		pseudo.put((byte) 0);
		pseudo.put((byte) protocol);
		pseudo.putShort((short) getNextHeader().getLength());
		
		target.setPseudoHeader(pseudo);
		
	}
	
	private int packFlagsFragment() {
		int n = fragmentOffset / 8;

		if(reserved) {
			n |= FL_RESERVED;
		}
		
		if(CE) {
			n |= FL_CE;
		}
		
		if(DF) {
			n |= FL_DF;
		}
		
		if(MF) {
			n |= FL_MF;
		}
		
		return n;
	}
	
	private static int nextId = 1;
	private void generateIdentification() {
		synchronized(IPv4.class) {
			identification = nextId++;
		}
	}
	
	@Override
	protected int minimumHeaderLength() {
		return 20;
	}
	
	private void unpackFlagsFragment(int value) {
		fragmentOffset = (value & FRAGMENT_MASK) * 8;
		reserved = ((value & FL_RESERVED) != 0);
		CE = ((value & FL_CE) != 0);
		DF = ((value & FL_DF) != 0);
		MF = ((value & FL_MF) != 0);
	}
	
	@Override
	public String toString() {
		StringBuffer out = new StringBuffer();
		out.append("IPv4 ");
		if (isFragment()) out.append("Fragment ");
		out.append(sourceAddress);
		out.append(" -> ");
		out.append(destinationAddress);
		if (getDF()) out.append(" (DF)");
		if (getMF()) out.append(" (MF)");
		if (isFragment())
			out.append(" offset="+getFragmentOffset()+" length="+getLength());
		return out.toString();
	}
	
	@Override
	public PacketPayload payload() {
		return new PacketPayload(headerBufferSlice(getHeaderLength()));
	}
	
	public void copyHeader(ByteBuffer writeBuffer) {
		ByteBuffer b = toByteBuffer();
		for(int i = 0; i < getHeaderLength(); i++) {
			writeBuffer.put(b.get(i));
		}
	}
}
