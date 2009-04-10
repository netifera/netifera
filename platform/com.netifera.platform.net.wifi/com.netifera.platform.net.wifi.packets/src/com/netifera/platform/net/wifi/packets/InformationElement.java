package com.netifera.platform.net.wifi.packets;

public class InformationElement {
	final private int id;
	final private byte[] bytes;
	
	public InformationElement(int id, byte[] bytes) {
		this.id = id;
		this.bytes = bytes;
	}
	
	public int id() {
		return id;
	}
	
	public byte[] toBytes() {
		return bytes;
	}
	
	public String toString() {
		return new String(bytes);
	}
	
	public Integer toInteger() {
		int answer = 0;
		for (byte b: bytes) {
			answer <<= 8;
			answer |= (b & 0xFF);
		}
		return answer;
	}

	public Long toLong() {
		long answer = 0;
		for (byte b: bytes) {
			answer <<= 8;
			answer |= (b & 0xFF);
		}
		return answer;
	}
}
