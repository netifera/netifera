package com.netifera.platform.net.packets;

import java.nio.ByteBuffer;

public interface IPacketHeader {
	
	/**
	 * Returns the length in bytes of this packet header.
	 * 
	 * @return
	 */
	int getHeaderLength();
	
	/**
	 * Returns the length of the entire packet starting at this header.
	 * This is the size of buffer necessary to call the
	 * {@link IPacketHeader#pack(ByteBuffer)} method.
	 * 
	 * @return
	 */
	int getLength();
	
	/**
	 * Returns the protocol value of the header encapsulated by this header or
	 * -1 if there is no such value.
	 * @return
	 */
	int getNextProtocol();
	
	/**
	 * Return the next header encapsulated by this header or <code>null</code>
	 * if this is the last header in the chain.
	 * 
	 * @return
	 */
	IPacketHeader getNextHeader();
	
	/**
	 * Return the previous header encapsulating this header or <code>null</code>
	 * if this is the first header in the chain.
	 * 
	 * @return
	 */
	IPacketHeader getPreviousHeader();

	/**
	 * Search all nested headers for a header instance of the type specified by
	 * <code>packetClass</code> and return it if it exists.  Otherwise return
	 * <code>null</code>.
	 * 
	 * @param packetClass
	 * @return
	 */
	IPacketHeader findHeader(Class<? extends IPacketHeader> headerClass);
	
	
	/**
	 * Returns a string representation of the entire chain of nested headers.
	 * 
	 * @return
	 */
	String print();
	
	/**
	 * 
	 * @param buffer A <code>ByteBuffer</code> with enough remaining space to
	 * store the entire packet starting at this header.
	 * @return This method will return false if there is insufficient space in
	 * <code>buffer</code> to store the entire packet.
	 */
	boolean pack(ByteBuffer buffer);
	
	/**
	 * Buffer position must be 0.  If this method succeeds and returns true,
	 * the buffer position will be updated by the size of the parsed header.
	 * If this method fails and returns false, the buffer position will not be
	 * changed.
	 * 
	 * @param buffer
	 * @return
	 */
	boolean unpack(ByteBuffer buffer);
	
	void setNextPacket(IPacketHeader packet);
	
	void setPreviousPacket(IPacketHeader packet);
	
	/**
	 * Prepare a chain of packet headers for storage outside of a packet
	 * handling callback.
	 * 
	 * <p>When decoding packets, the ByteBuffer which is passed to
	 * {@link #unpack(ByteBuffer)} and which is stored by each header is backed
	 * by the array of bytes into which the packet data was originally read from
	 * the network.</p>
	 * 
	 * <p>To make packet reception and decoding as efficient as possible,
	 * copying from this array is avoided whenever possible. This means that it
	 * is only safe to examine the contents of this array and the ByteBuffer
	 * objects that wrap it during callback handling of the packet.
	 * When the packet handling callback returns, a new packet will be read into
	 * the same byte array and the contents of the ByteBuffer objects referenced
	 * by the headers will no longer be valid.</p>
	 * 
	 * <p>In most cases, this is not a problem because the contents of the array
	 * are decoded into fields in the packet header when the header is initially
	 * unpacked, and then the array data (via the ByteBuffer objects) is never
	 * examined again.</p>
	 * 
	 * <p>Headers such as <code>PacketPayload</code> refer to the array backed
	 * data directly and need to make a local copy if they are to be stored
	 * longer after the packet handling callback returns.</p>
	 */
	void persist();
	
	ByteBuffer toByteBuffer();
	
	/**
	 * Create a new packet with the current packet as payload.
	 * 
	 * @return
	 */
	IPacketHeader payload();
}
