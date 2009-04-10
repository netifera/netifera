package com.netifera.platform.net.packets.util;

import java.nio.ByteBuffer;

public class PacketChecksum {
	
	public static int rawSum(ByteBuffer buffer) {
		
		int sum = 0;
		
		while(buffer.remaining() > 1) {
			sum +=  (((buffer.get() & 0xFF) << 8) | (buffer.get() & 0xFF) & 0xFFFF);
		}
		
		if(buffer.remaining() == 1) {
			sum += ((buffer.get() & 0xFF) << 8);
		}
		
		return sum;		
	}
	
	public static int reduceSum(int sum) {
		while(sum > 0xFFFF) {
			sum = (sum & 0xFFFF) + (sum  >>> 16);
		}
		
		return (~sum & 0xFFFF);
	}
	
	public static int checksum(ByteBuffer buffer) {
		return reduceSum( rawSum(buffer) );
	}

}
