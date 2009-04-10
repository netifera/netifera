package com.netifera.platform.net.packets;

import java.nio.ByteBuffer;

import com.netifera.platform.net.packets.util.PacketChecksum;

/**
 * 
 *
 *
 */
public abstract class AbstractPacket implements IPacketHeader {
	
	/*
	 * Used in persist() to invalidate headerBuffer.  Easier to diagnose if headerBuffer is
	 * accessed after persist() is called.
	 */
	private final static ByteBuffer nullBuffer = ByteBuffer.allocate(0).asReadOnlyBuffer();
	
	/* The next nested header, or null if this is the last header in the chain */
	private IPacketHeader nextHeader;
	private IPacketHeader previousHeader;
	
	/* The buffer this header was unpacked from or packed into.  null if this header is not yet packed */
	private ByteBuffer headerBuffer;
	
	protected AbstractPacket() {}
	
	protected AbstractPacket(IPacketHeader next) {
		this.nextHeader = next;
	}
	
	protected AbstractPacket(IPacketHeader prev, IPacketHeader next) {
		this.previousHeader = prev;
		this.nextHeader = next;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netifera.platform.net.packets.IPacketHeader#getNextHeader()
	 */
	final public IPacketHeader getNextHeader() {
		return nextHeader;
	}
	
	final public IPacketHeader getPreviousHeader() {
		return previousHeader;
	}
	
	final public int getNextProtocol() {
		return nextProtocol();
	}
	
	/**
	 * Subclasses which encapsulate other protocol headers must override this
	 * method to return an appropriate protocol constant for the next nested header.
	 * 
	 * The default implementation returns -1 which indicates that this header does
	 * not encapsulate another protocol.
	 * 
	 * @return The protocol constant for the next nested header or -1 if this header
	 * does not encapsulate another protocol.
	 */
	protected int nextProtocol() {
		return -1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netifera.platform.net.packets.IPacketHeader#getHeaderLength()
	 */
	final public int getHeaderLength() {
		return headerLength();
	}
	
	/**
	 * Return the minimum size for this type of header.  This method must be implemented by
	 * subclasses to return the minimum size in bytes of the protocol header.
	 * @return Minimum size in bytes of the protocol header.
	 */
	protected abstract int minimumHeaderLength();

	/**
	 * Default implementation simply delegates to minimumHeaderLength() which is the same
	 * value for fixed sized headers.  Header implementations with variable length should override
	 * this method and dynamically calculate the length of the header based on field values.
	 * @return
	 */
	protected int headerLength() {
		return minimumHeaderLength();
	}
	
	
	final protected int remaining() {
		return headerBuffer.remaining();
	}

	/**
	 * 
	 * @return
	 */
	final protected ByteBuffer headerBufferSlice() {
		return headerBuffer.slice();
	}

	final protected ByteBuffer headerBufferSlice(int length) {
		ByteBuffer b = headerBuffer.duplicate();
		b.position(length);
		return b.slice();
	}

	public int getLength() {
		if(nextHeader != null) {
			return nextHeader.getLength() + getHeaderLength();
		}
		
		return getHeaderLength();
	}
		
	//  PACKING
	
	final public boolean pack(ByteBuffer buffer) {
		
		if(buffer.remaining() < getHeaderLength()) {
			return false;
		}
		
		headerBuffer = buffer;
		
		populateGeneratedFields();
		
		packHeader();
		
		/*
		 * We must call this first because checksums may be calculated over these bytes
		 */
		if(nextHeader != null) {
			/*
			 * The header for 'this' has already been packed, so the headerBuffer 'position' points to the beginning
			 * of the header for nextPacket.
			 */
			if(!nextHeader.pack(headerBuffer.slice())) {
				return false;
			}
		}
		
		calculateChecksum();
		
		return true;
	}
	
	
	protected void populateGeneratedFields() {
		// default empty implementation
	}
	
	/**
	 * Subclasses must implement this method to pack the fields of the header.
	 * 
	 * <ul>
	 *   <li><p>{@link #pack8(int)} Packs a single 8 bit byte.</p></li>
	 *   <li><p>{@link #pack16(int)} Packs a 16 bit field in network byte order.</p></li>
	 *   <li><p>{@link #pack32(int)} Packs a 32 bit field in network byte order.</p></li>
	 *   <li><p>{@link #packBytes(byte[])} Packs a contiguous array of bytes. </p></li>
	 * </ul>
	 */
	abstract protected void packHeader();

	/**
	 * Store a single 8 bit byte value at the current header buffer position and increment the position by 1 byte.
	 * 
	 * @param value  Byte value to be stored.
	 */
	final protected void pack8(int value) {
		headerBuffer.put((byte) value);
	}
	
	/**
	 * Store a 16 bit value in network byte order at the current header buffer offset and increment the position by 2 bytes.
	 * 
	 * @param value 16 bit value to be stored.
	 */
	final protected void pack16(int value) {
		headerBuffer.putShort((short) value);
	}
	
	/**
	 * Store a 32 bit value in network byte order at the current header buffer position and increment the position by 4 bytes.
	 * 
	 * @param value
	 */
	final protected void pack32(int value) {
		headerBuffer.putInt(value);
	}
	
	/**
	 * Store an array of bytes at the current header buffer position and increment the position by the length of the array.  The number of bytes
	 * stored is equal to the length of the array.
	 * 
	 * @param data Array of bytes to store.
	 */
	final protected void packBytes(byte[] data) {
		headerBuffer.put(data);
	}

	/**
	 * Store a single 8 bit byte value at the specified absolute offset from the start of this header.  The buffer position is not changed.
	 * @param value Byte value to be stored.
	 * @param offset Absolute offset in bytes from the start of this header.
	 */
	final protected void pack8(int value, int offset) {
		headerBuffer.put(offset, (byte) value);
	}
		
	/**
	 * Store a 16 bit value in network byte order at the specified absolute offset from the start of this header.  The buffer position is not changed.
	 * @param value 16 bit value to be stored.
	 * @param offset Absolute offset in bytes from the start of this header.
	 */
	final protected void pack16(int value, int offset) {
		headerBuffer.putShort(offset, (short) value);
	}
	
	/**
	 * Store a 32 bit value in network byte order at the specified absolute offset from the start of this header.  The buffer position is not changed.
	 * @param value 32 bit value to be stored.
	 * @param offset Absolute offset in bytes from the start of this header.
	 */
	final protected void pack32(int value, int offset) {
		headerBuffer.putInt(offset, value);
	}
	
	static public int swap16(int n) {
		return  ((n & 0xFF00) >> 8) | ((n & 0xFF) << 8);
	}
	
	static public int swap32(int n) {
		return ((n & 0xFF) << 24)
			| ((n & 0xFF00) << 8)
			| ((n & 0xFF0000) >> 8)
			| ((n >>> 24) & 0xFF);
	}
	
	final protected static void verifyMaximum(int value, int maximum) {
		if((value < 0) || (value > maximum)) {
			throw new PacketFieldException(value);
		}
	}
	
	
	//  CHECKSUMS
	
	/**
	 * Fill in any required checksums after packing a header.  The default implementation does nothing.
	 * Override this method to implement checksum calculation.
	 */
	protected void calculateChecksum() {
	}

	
	
	/**
	 * 
	 * @param data
	 * @param pseudoHeader
	 * @return
	 */
	final protected int generateChecksumWithPseudo(ByteBuffer data, ByteBuffer pseudoHeader) {
		int raw = PacketChecksum.rawSum(pseudoHeader);
		raw += PacketChecksum.rawSum(data);
		return PacketChecksum.reduceSum(raw);
	}
	
	/**
	 * 
	 * @param length
	 * @param pseudoHeader
	 * @return
	 */
	final protected int generateChecksumWithPseudo(int length, ByteBuffer pseudoHeader) {
		ByteBuffer b = headerBuffer.duplicate();
		b.limit(length);
		b.rewind();
		int raw = PacketChecksum.rawSum(pseudoHeader);
		raw += PacketChecksum.rawSum(b);
		return PacketChecksum.reduceSum(raw);
	}
	
	/**
	 * 
	 * @param length
	 * @return
	 */
	final protected int generateChecksum(int length) {
		ByteBuffer b = headerBuffer.duplicate();
		b.limit(length);
		b.rewind();
		return generateChecksum(b);
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	final protected int generateChecksum(ByteBuffer data) {
		return PacketChecksum.checksum(data);
	}
	
	 //  UNPACKING
	
	private boolean isUnpacked;

	public void setNextPacket(IPacketHeader payload) {
		if(!isUnpacked) {
			// FIXME should we allow this?
		}
		
		this.nextHeader = payload;
	}

	public void setPreviousPacket(IPacketHeader payload) {
		if(!isUnpacked) {
			// FIXME should we allow this?
		}
		
		this.previousHeader = payload;
	}
	
	public boolean unpack(ByteBuffer buffer) {
		if(buffer.position() != 0) {
			throw new IllegalArgumentException("The unpack() method must be called with buffer.position() == 0");
		}
		
		if(buffer.remaining() < minimumHeaderLength()) {
			return false;
		}
		headerBuffer = buffer;
		unpackHeader();
		if(!isValidHeader()) {
			buffer.rewind();
			return false;
		}
		if(hasPayload() && headerBuffer.hasRemaining()) {
			nextHeader = new PacketPayload(headerBuffer.slice());
			buffer.position(buffer.limit());
		}
		isUnpacked = true;
		return true;
	}

	/**
	 * This method must be implemented by subclasses to unpack the header
	 * buffer into the fields of the packet.  When this method is called,
	 * the unpackX()
	 * 
	 * 	 * point past the end of this he
	 * <ul>
	 *   <li><p> {@link #unpack8()} </p></li>
	 *   <li><p> {@link #unpack16()} </p></li>
	 *   <li><p> {@link #unpack32()} </p></li>
	 *   <li><p> {@link #unpack64()} </p></li>
	 *   <li><p> {@link #unpackBytes(int)} </p></li>
	 * </ul>
	 */
	protected abstract void unpackHeader();
	
	
	/**
	 * Override to validate header fields
	 * @return True if header is valid
	 */
	protected boolean isValidHeader() {
		return true;
	}
	
	/**
	 * Override to return true for automatic creation of PacketPayload while unpacking header.
	 * @return
	 */
	protected boolean hasPayload() {
		return false;
	}
	
	
	
	
	
	protected long unpack64() {
		return headerBuffer.getLong();
	}
	
	protected int unpack32() {
		return headerBuffer.getInt();
	}
	
	protected int unpack16() {
		return headerBuffer.getShort() & 0xFFFF;
	}
	
	protected int unpack8() {
		return headerBuffer.get() & 0xFF;
	}
	
	protected byte[] unpackBytes(int length) {
		byte[] data = new byte[length];
		headerBuffer.get(data);
		return data;
	}
	
	public String print() {
		if(nextHeader != null) {
			return toString() + " + " + nextHeader.print();
		}
		return toString();
	}
	
	public final void persist() {
		persistData();
		headerBuffer = nullBuffer;
		if(nextHeader != null) {
			nextHeader.persist();
		}
	}
	
	protected void persistData() {
		// default empty implementation
	}
	
	public PacketPayload payload() {
		return new PacketPayload(headerBuffer.slice());
	}
	
	public ByteBuffer toByteBuffer() {
		return headerBuffer.duplicate();
	}
	
	public IPacketHeader findHeader(Class<? extends IPacketHeader> packetClass) {
		if (packetClass.isInstance(this)) return this;
		if (getNextHeader() == null) return null;
		return nextHeader.findHeader(packetClass);
	}

}
