package com.netifera.platform.net.packets.util;

// FIXME check buflen before pack
//      else java.lang.ArrayIndexOutOfBoundsException at FieldPacker.bufferPack()

@Deprecated // not used: remove from codebase?
public class FieldPacker {
	
	private byte[] dataBuffer;
	private int bufferLength;
	private int baseOffset;
	private int current;
	
	static public void bufferPack16(byte[] buffer, int offset, int value) {
		buffer[offset++] = (byte)(value >> 8);
		buffer[offset] = (byte)(value);
	}
	
	static public void bufferPack32(byte[] buffer, int offset, int value) {
		buffer[offset++] = (byte)(value >> 24);
		buffer[offset++] = (byte)(value >> 16);
		buffer[offset++] = (byte)(value >> 8);
		buffer[offset++] = (byte)(value);
	}
	
	static public int bufferUnpack16(byte[] buffer, int offset) {
		int value = 0; 
		value |= (buffer[offset++] & 0xFF);
		value <<= 8;
		value |= (buffer[offset] & 0xFF);
		return value;
	}
	
	static public int bufferUnpack32(byte[] buffer, int offset) {
		int value = 0; 

		value |= (buffer[offset++] & 0xFF);
		value <<= 8;
		value |= (buffer[offset++] & 0xFF);
		value <<= 8;
		value |= (buffer[offset++] & 0xFF);
		value <<= 8;
		value |= (buffer[offset] & 0xFF);
		
		return value;
	}
	
	static public long bufferUnpack64(byte[] buffer, int offset) {
		long value = 0; 

		value |= (buffer[offset++] & 0xFF);
		value <<= 8;
		value |= (buffer[offset++] & 0xFF);
		value <<= 8;
		value |= (buffer[offset++] & 0xFF);
		value <<= 8;
		value |= (buffer[offset++] & 0xFF);
		value <<= 8;
		value |= (buffer[offset++] & 0xFF);
		value <<= 8;
		value |= (buffer[offset++] & 0xFF);
		value <<= 8;
		value |= (buffer[offset++] & 0xFF);
		value <<= 8;
		value |= (buffer[offset] & 0xFF);
		
		return value;
	}
		
	public void initialize(byte[] buffer, int offset, int length) {
		
		this.dataBuffer = buffer.clone();
		this.bufferLength = length;
		this.baseOffset = offset;
		this.current = 0;
	}
	
	public void pack16(int value) {
		bufferPack16(dataBuffer, baseOffset + current, value);
		current += 2;
	}
	
	public void pack32(int value) {
		bufferPack32(dataBuffer, baseOffset + current, value);
		current += 4;
	}
	
	public void pack8(int value) {
		dataBuffer[baseOffset + current++] = (byte)(value);
	}
	
	void packBytes(byte[] data, int offset, int length) {
		System.arraycopy(data, offset, dataBuffer, baseOffset + current, length);
		current += length;
	}
	
	public void packBytes(byte[] data) {
		packBytes(data, 0, data.length);
	}
	
	public long unpack64() {
		long value = bufferUnpack64(dataBuffer, baseOffset + current);
		current += 8;
		return value;
	}
	
	public int unpack32() {
		int value = bufferUnpack32(dataBuffer, baseOffset + current);
		current += 4;
		return value;
	}
	
	public int unpack16() {
		int value = bufferUnpack16(dataBuffer, baseOffset + current);
		current += 2;
		return value;
	}
	
	public int unpack8() {
		return (dataBuffer[baseOffset + current++] & 0xFF);
	}
	
	public void unpackBytes(byte[] buffer, int length) {
		unpackBytes(buffer, 0, length);
	}
	
	void unpackBytes(byte[] buffer, int offset, int length) {
		System.arraycopy(dataBuffer, baseOffset + current, buffer, offset, length);
		current += length;
	}
	
	/** Current offset from base offset */
	public int getCurrentOffset() {
		return current;
	}

	public int getSpaceLeft() {
		return bufferLength - (baseOffset + current);
	}



}
