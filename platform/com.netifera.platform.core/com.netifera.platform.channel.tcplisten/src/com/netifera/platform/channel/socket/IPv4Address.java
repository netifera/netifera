package com.netifera.platform.channel.socket;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPv4Address {
	
	private int addressData;
	final public static String IPv4AddressRegex = "(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}";

	public IPv4Address(int data) {
		this.addressData = data;
	}
	
	public static boolean isValid(final String ipString) {
		return ipString.matches(IPv4AddressRegex);
	}
	
    public static int stringParse(String ipString) {
		
		if(!isValid(ipString)) {
			return 0;
		}
				
		String[] parts = ipString.split("\\.");
		
		int[] shifts = { 24, 16, 8, 0 };
		int i = 0;
		int address = 0;

		for(String s : parts) {
			address |= ( Integer.parseInt(s) << shifts[i++] );				
		}
		
		return address;			
	}
	public byte[] toBytes() {
		byte answer[] = new byte[4];
		
		answer[0] = (byte)((addressData >> 24) & 0xFF);
		answer[1] = (byte)((addressData >> 16) & 0xFF);
		answer[2] = (byte)((addressData >> 8) & 0xFF);
		answer[3] = (byte)(addressData & 0xFF);

		return answer;
	}
	
	public InetAddress getInetAddress() {
		try {
			return InetAddress.getByAddress(toBytes());
		} catch (UnknownHostException e) {
			// should not happen(?)
			throw new RuntimeException("Unexpected exception", e);
		}
	}
}
