package com.netifera.platform.internal.system;

import java.nio.ByteOrder;

public class Bytesex {
	
	private final boolean nativeBigEndian;
	private final int[] index16; /* MSB to LSB */
	private final int[] index32; /* MSB to LSB */
	public Bytesex() {
		
		nativeBigEndian = (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
			
		if(nativeBigEndian) {
			index16 = new int[] { 0, 1 };
			index32 = new int[] { 0, 1, 2, 3 };	
		} else {
			index16 = new int[] { 1, 0 };
			index32 = new int[] { 3, 2, 1, 0 };
		}
	}
	
	public int unpack32(byte[] data, int offset) {
		int ret = 0;
		
		ret |= (data[offset + index32[0]] & 0xFF);
		ret <<= 8;
		ret |= (data[offset + index32[1]] & 0xFF);
		ret <<= 8;
		ret |= (data[offset + index32[2]] & 0xFF);
		ret <<= 8;
		ret |= (data[offset + index32[3]] & 0xFF);
		
		return ret;
	}
		
	public int unpack16(byte[] data, int offset) {
		int ret = 0;
		
		ret |= (data[offset + index16[0]] & 0xFF);
		ret <<= 8;
		ret |= (data[offset + index16[1]] & 0xFF);
		
		return ret;
	}

	public void pack32(byte[] data, int offset, int value) {
		data[offset + index32[0]] = (byte) (value >> 24);
		data[offset + index32[1]] = (byte) (value >> 16);
		data[offset + index32[2]] = (byte) (value >> 8);
		data[offset + index32[3]] = (byte) (value);
	}
	
	public void pack16(byte[] data, int offset, int value) {
		data[offset + index16[0]] = (byte) (value >> 8);
		data[offset + index16[1]] = (byte) (value);
	}
	

	static public int swap16(int n) {
		return  ((n & 0xFF00) >> 8) | ((n & 0xFF) << 8);
	}
	
	static public int swap32(int n) {
		return ((n & 0xFF) << 24) | 
		((n & 0xFF00) << 8) |
		((n & 0xFF0000) >> 8) |
		((n >>> 24) & 0xFF);
		
	}

	public int ntohs(int n) {
		if(!nativeBigEndian) {
			return swap16(n);
		} else {
			return n;
		}
	}
	public int htons(int n) {
		if(!nativeBigEndian) {
			return swap16(n);
		} else {
			return n;
		}
	}
	public int ntohl(int n) {
		if(!nativeBigEndian) {
			return swap32(n);
		} else {
			return n;
		}
		
	}

	public int htonl(int n) {
		if(!nativeBigEndian) {
			return swap32(n);
		} else {
			return n;
		}
	}
}

