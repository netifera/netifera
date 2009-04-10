package com.netifera.platform.net.packets;

import java.nio.ByteBuffer;

public class PacketPayload implements IPacketHeader {
	
	public static final PacketPayload EMPTY_PAYLOAD = new PacketPayload(ByteBuffer.allocate(0).asReadOnlyBuffer());
	//static private final HexDump hexdump = new HexDump();
	
	private IPacketHeader previousHeader;

	static public PacketPayload emptyPayload() {
		return EMPTY_PAYLOAD;
	}
	
	private ByteBuffer payloadBuffer;

	/*
	public PacketPayload() {
		// XXX NPE this.payloadBuffer.*
	}
	*/
	
	public PacketPayload(ByteBuffer buffer) {
		this.payloadBuffer = buffer;
	}
	
	public PacketPayload payload() {
		return this;
	}
	
	public int getHeaderLength() {
		return payloadBuffer.limit();
	}
	
	public int getLength() {
		return payloadBuffer.limit();
	}

	public IPacketHeader getPreviousHeader() {
		return previousHeader;
	}
	
	public IPacketHeader getNextHeader() {
		return null;
	}

	public int getNextProtocol() {
		return -1;
	}

	public boolean pack(ByteBuffer buffer) {
		payloadBuffer.rewind();
		if(buffer.remaining() < payloadBuffer.limit()) {
			return false;
		}
		
		buffer.put(payloadBuffer);
		return true;
	}

	public boolean unpack(ByteBuffer buffer) {
		payloadBuffer = buffer;
		return true;
	}
	
	
	public String print() {
		return toString();
	}
	
	public void persist() {
		ByteBuffer tmp = ByteBuffer.allocate(payloadBuffer.limit());
		payloadBuffer.rewind();
		tmp.put(payloadBuffer);
		payloadBuffer = tmp;
	}
	
	@Override
	public String toString() {
		return "Payload (" + payloadBuffer.limit() + " bytes)";// + hexdump.bufferDump(payloadBuffer.slice());
	}
	
	public ByteBuffer toByteBuffer() {
		return payloadBuffer.duplicate();
	}
	
	public byte[] toBytes() {
		ByteBuffer buffer = toByteBuffer();
		byte[] answer = new byte[buffer.limit()];
		buffer.get(answer);
		return answer;
	}
	
	public IPacketHeader findHeader(Class<? extends IPacketHeader> packetClass) {
		if (packetClass.isInstance(this)) return this;
		return null;
	}
	
	public void setPreviousPacket(IPacketHeader packet) {
		previousHeader = packet;
	}
	
	public void setNextPacket(IPacketHeader packet) {
		throw new IllegalArgumentException("Cannot call setNextPacket() on PacketPayload");
	}
}
