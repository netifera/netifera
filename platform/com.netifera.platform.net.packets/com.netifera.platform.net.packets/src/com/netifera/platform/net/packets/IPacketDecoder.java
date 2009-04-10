package com.netifera.platform.net.packets;

import java.nio.ByteBuffer;

/**
 * An <code>IPacketDecoder</code> implementation knows how to decode a ByteBuffer containing the raw bytes of a 
 * network packet into a network header of a particular type.  It is also responsible for examining 
 * the value of {@link IPacketHeader#getNextProtocol()} and creating nested headers by creating header 
 * instances or invoking {@link IPacketDecoder#decode(ByteBuffer)} on another decoder.
 */
public interface IPacketDecoder {
	/**
	 *  The buffer position points to the first byte to decode and the remaining count indicates how many bytes to
	 *  decode in this buffer.  The position is not required to initially be 0 and the capacity of the buffer
	 *  may be larger than the limit.  Only the bytes between position and limit will be decoded.
	 *  
	 * @param buffer
	 * @return The first header in the chain of decoded headers.  Never returns <code>null</code>.
	 */
	IPacketHeader decode(ByteBuffer buffer);
}
