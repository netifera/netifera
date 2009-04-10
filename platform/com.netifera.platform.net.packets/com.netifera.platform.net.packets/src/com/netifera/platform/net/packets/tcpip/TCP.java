package com.netifera.platform.net.packets.tcpip;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketException;
import com.netifera.platform.net.packets.PacketFieldException;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.util.NetworkConstants;

public class TCP extends AbstractPacket implements IPseudoHeaderClient, IPv4Encapsulable, IPv6Encapsulable {
	private int sourcePort;
	private int destinationPort;
	private TCPSequenceNumber sequenceNumber;
	private TCPSequenceNumber acknowledgementNumber;
	private int dataOffset;
	
	public final static int  CWR = 7;
	public final static int  ECE = 6;
	public final static int  URG = 5;
	public final static int  ACK = 4;
	public final static int  PSH = 3;
	public final static int  RST = 2;
	public final static int  SYN = 1;
	public final static int  FIN = 0;
	
	public final static int OPT_EOL			= 0;
	public final static int OPT_NOP			= 1;
	public final static int OPT_MAXSEG		= 2;
	public final static int OPT_WSCALE		= 3;
	public final static int OPT_SACKOK		= 4;
	public final static int OPT_TIMESTAMP 	= 8;
	
	private int x2;
	private int flags;
	
	private int window;
	private int checksum;
	private int urgentPointer;
	
	private List<byte[]> options;
	private int optionsLength;
	private boolean optionsEnded;
	private final static int MAXIMUM_OPTIONS_LENGTH = 40;
	
	private final static byte[] emptyOptions = new byte[0];
	private byte[] optionsBuffer = emptyOptions;
	
	private boolean isValidUnpack;
	
	/**

    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |          Source Port          |       Destination Port        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                        Sequence Number                        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                    Acknowledgment Number                      |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |  Data |           |U|A|P|R|S|F|                               |
   | Offset| Reserved  |R|C|S|S|Y|I|            Window             |
   |       |           |G|K|H|T|N|N|                               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |           Checksum            |         Urgent Pointer        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                    Options                    |    Padding    |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                             data                              |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

                            TCP Header Format                            
	 */
	@Override
	protected void packHeader() {		
		if( (optionsLength > 0) && (!optionsEnded)) {
			addOptionEOL();
		}
		pack16(sourcePort);
		pack16(destinationPort);
		pack32(sequenceNumber.toInteger());
		pack32(acknowledgementNumber.toInteger());
		pack8(dataOffset << 4);
		pack8(flags);
		pack16(window);
		pack16(checksum);
		pack16(urgentPointer);
		packOptions();
	}
	
	@Override
	protected void unpackHeader() {
		sourcePort				= unpack16();
		destinationPort			= unpack16();
		sequenceNumber			= new TCPSequenceNumber(unpack32());
		acknowledgementNumber	= new TCPSequenceNumber(unpack32());
		final int n 			= unpack8();
		dataOffset 				= (n >> 4);
		x2 						= (n & 0xF);
		flags					= unpack8();
		window					= unpack16();
		checksum				= unpack16();
		urgentPointer			= unpack16();
		
		final int optionsLength = (dataOffset - 5) * 4;
	
		if(optionsLength > 0) {
			if(remaining() < optionsLength) {
				isValidUnpack = false;
				return;
			}
			optionsBuffer = unpackBytes(optionsLength);
		}
		isValidUnpack = true;
	}
	
	@Override
	protected boolean isValidHeader() {
		return isValidUnpack;
	}
	
	@Override
	protected boolean hasPayload() {
		return true;
	}
	
	private void packOptions() {
		if(optionsLength <= 0) 
			return;
				
		for(byte[] op : options) {
			packBytes(op);
		}
	}
	
	private boolean checksumSet;
	private ByteBuffer pseudoHeader;
	
	public TCP() {}
	
	public TCP(IPacketHeader next) {
		super(next);
		
		sequenceNumber = new TCPSequenceNumber(0);
		acknowledgementNumber = new TCPSequenceNumber(0);
		
		dataOffset = 5;
	}
	
	public void setSourcePort(int value) {
		verifyMaximum(value, 0xFFFF);
		sourcePort = value;
	}
	
	public int getSourcePort() {
		return sourcePort;
	}
	
	public void setDestinationPort(int value) {
		verifyMaximum(value, 0xFFFF);
		destinationPort = value;
	}
	
	public int getDestinationPort() {
		return destinationPort;
	}
	
	public void setSequenceNumber(TCPSequenceNumber value) {
		sequenceNumber = value;
	}
	
	public TCPSequenceNumber getSequenceNumber() {
		return sequenceNumber;
	}
	
	public void setAcknowledgementNumber(TCPSequenceNumber value) {
		acknowledgementNumber = value;
	}
	
	public TCPSequenceNumber getAcknowledgementNumber() {
		return acknowledgementNumber;
	}
	
	public void setDataOffset(int value) {
		verifyMaximum(value, 0x0F);
		dataOffset = value;
	}
	
	public int getDataOffset() {
		return dataOffset;
	}
	
	public int getReserved() {
		return x2;
	}
	
	public void setFlag(int index, boolean value) {
		assert( (index >= 0) && (index <= 7) );
		
		int bit = (1 << index);
		
		if(value) {
			flags |= bit;
		} else {
			flags &= ~bit;
		}
	}
	
	private boolean getFlag(int index) {
		assert( (index >= 0) && (index <= 7) );
		int bit = (1 << index);
		return ((flags & bit) != 0);
	}
	
	public int getFlags() {
		return flags;
	}
	
	public void setCWR(boolean value) {
		setFlag(CWR, value);
	}
	
	public boolean getCWR() {
		return getFlag(CWR);
	}
	
	public void setECE(boolean value) {
		setFlag(ECE, value);
	}
	
	public boolean getECE() {
		return getFlag(ECE);
	}
	
	public void setURG(boolean value) {
		setFlag(URG, value);
	}
	
	public boolean getURG() {
		return getFlag(URG);
	}
	
	public void setACK(boolean value) {
		setFlag(ACK, value);
	}
	
	public boolean getACK() {
		return getFlag(ACK);
	}
	
	public void setPSH(boolean value) {
		setFlag(PSH, value);
	}
	
	public boolean getPSH() {
		return getFlag(PSH);
	}
	
	public void setRST(boolean value) {
		setFlag(RST, value);
	}
	
	public boolean getRST() {
		return getFlag(RST);
	}
	
	public void setSYN(boolean value) {
		setFlag(SYN, value);
	}
	
	public boolean getSYN() {
		return getFlag(SYN);
	}
	
	public void setFIN(boolean value) {
		setFlag(FIN, value);
	}
	
	public boolean getFIN() {
		return getFlag(FIN);
	}

	public void setWindow(int value) {
		verifyMaximum(value, 0xFFFF);
		window = value;
	}
	
	public int getWindow() {
		return window;
	}

	public void setChecksum(int value) {
		verifyMaximum(value, 0xFFFF);
		checksum = value;
		checksumSet = true;
	}
	
	public int getChecksum() {
		return checksum;
	}
	
	public void setUrgentPointer(int value) {
		verifyMaximum(value, 0xFFFF);
		urgentPointer = value;
	}
	
	public int getUrgentPointer() {
		return urgentPointer;
	}
	
	public byte[] getOptionsBuffer() {
		return optionsBuffer.clone();
	}
	
	public List<byte[]> getOptions() {
		return options;
	}
	
	private void optionCheck(int size) {
		if(optionsEnded) {
			throw new PacketFieldException("Cannot add option to closed option list");
		}
		
		if(optionsLength + size >= MAXIMUM_OPTIONS_LENGTH) {
			throw new PacketFieldException("No space to add TCP option");
		}	
	}
	
	public void addOption(byte[] op) {
		if(op == null)
			return;
		
		optionCheck(op.length);
		optionsLength += op.length;
		getOptionList().add(op);
	}
	
	public void addOptionMSS(int value) {
		byte[] op = new byte[4];
		op[0] = 2;
		op[1] = 4;
		op[2] = (byte)(value >> 8);
		op[3] = (byte)(value);

		addOption(op);
	}
	
	public void addOptionWindowScale(int value) {
		
		byte[] op = new byte[3];
		op[0] = 3;
		op[1] = 3;
		op[2] = (byte)value;
		
		addOption(op);
	}
	
	public void addOptionNOP() {

		byte[] op = new byte[1];
		op[0] = 1; 				/* kind = NOP */
		
		addOption(op);
	}
	
	public void addOptionEOL() {
		/*
		 * Pad end of options with between 1 and 4
		 * zero (End of option list) bytes.
		 */
		int padCount = (4 - (optionsLength % 4));
		byte[] op = new byte[padCount];
		optionsLength += padCount;
		assert(optionsLength <= MAXIMUM_OPTIONS_LENGTH);
		getOptionList().add(op);
		optionsEnded = true;
		dataOffset = 5 + (optionsLength / 4); 
	}
	
	private List<byte[]> getOptionList() {
		if(options == null) {
			options = new ArrayList<byte[]>();
		}
		return options;
	}
	
	public void clearOptions() {
		getOptionList().clear();
		optionsLength = 0;
		optionsEnded = false;
	}
	
	public int protocolOverIPv4() {
		return NetworkConstants.IPPROTO_TCP;
	}
	
	public int protocolOverIPv6() {
		return NetworkConstants.IPPROTO_TCP;
	}
	
	@Override
	public int minimumHeaderLength() {
		return 20;
	}

	@Override
	public int headerLength() {
		if( (optionsLength > 0) && !optionsEnded) {
			throw new PacketException("Cannot calculate header length until options are closed");
		}
		return dataOffset * 4;
	}
	
	@Override
	public void populateGeneratedFields() {
		if(!checksumSet) {
			checksum = 0;
		}
	}
	
	@Override
	protected void calculateChecksum() {
		if(checksumSet) return;
	
		if(pseudoHeader == null) {
			throw new PacketException("Cannot calculated TCP checksum because parent has not sent pseudoheader");
		}
		
		int sum = generateChecksumWithPseudo(getLength(), pseudoHeader);
		pack16(sum, 16);
	}
	
	public void setPseudoHeader(ByteBuffer pseudoHeader) {
		this.pseudoHeader = pseudoHeader;
	}
	
	@Override
	public String toString() {
		StringBuffer out = new StringBuffer();
		out.append("TCP ");
		out.append(sourcePort);
		out.append(" -> ");
		out.append(destinationPort);
		
		if (getFIN() || getSYN() || getRST() || getLength() > 0) {
			out.append(" ");
			out.append(sequence());
			out.append(":");
			out.append(lastSequence());
			out.append(" (");
			out.append(getLength() - getHeaderLength());
			out.append(")");
		}
		if (getFIN()) out.append(" fin");
		if (getPSH()) out.append(" psh");
		if (getRST()) out.append(" rst");
		if (getSYN()) out.append(" syn");
		if (getURG()) out.append(" urg");
//		if (getXXX()) out.append(" XXX");
//		if (getYYY()) out.append(" YYY");

		if (getACK()) {
			out.append(" ack ");
			out.append(ackSequence());
		}

		out.append(" win ");
		out.append(getWindow());
		// TODO print options
		return out.toString();
	}

	public TCPSequenceNumber sequence() {
		return getSequenceNumber();
	}
	
	public TCPSequenceNumber ackSequence() {
		return getAcknowledgementNumber();
	}
	
	// Answer the last sequence number for this packet
	public TCPSequenceNumber lastSequence() {
		TCPSequenceNumber answer = sequence().add(getLength() - getHeaderLength() - 1);
		if (getSYN())
			answer = answer.next();
		if (getFIN())
			answer = answer.next();
		return answer;
	}
	
	public TCPSequenceNumber nextSequence() {
		return lastSequence().next();
	}
	
	@Override
	public PacketPayload payload() {
		return getNextHeader() == null ? PacketPayload.EMPTY_PAYLOAD : (PacketPayload) getNextHeader();
	}
}
