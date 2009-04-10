package com.netifera.platform.util.addresses.inet;

import java.net.InetAddress;

import com.netifera.platform.util.addresses.AddressFormatException;
import com.netifera.platform.util.addresses.INetworkAddress;
import com.netifera.platform.util.addresses.NetworkFamily;
import com.netifera.platform.util.patternmatching.InternetAddressMatcher;

public class IPv4Address extends InternetAddress {
	
	private static final long serialVersionUID = 6430077586662028819L;
	
	public static final int BYTESLENGTH = 4;
	
	/* 4 groups of 3 decimal digits (up to '255'),
	 * each group separated by a dot '.' */
	public static final int MAX_TEXTUAL_LENGTH = 15; // 3 * BYTESLENGTH + 3
	
	final int addressData;
	
	public static final IPv4Address any = new IPv4Address("0.0.0.0"); // unspecified
	public static final IPv4Address loopback = new IPv4Address("127.0.0.1");
	
	final public NetworkFamily getNetworkFamily() {
		return NetworkFamily.AF_INET;
	}
	
	public int getDataSize() {
		return BYTESLENGTH * 8;
	}
	
	public IPv4Address(int data) {
		super();
		addressData = data;
	}
	
	/**
	 * @param ipString the IP address
	 * 
	 * @exception AddressFormatException
	 */
	public IPv4Address(String ipString) {
		this(stringParse(ipString));
	}
	
	/**
	 * @param bytes IPv4 address in network byte order, the array must be at
	 * least four bytes long
	 * 
	 * @exception AddressFormatException if the array is less than four bytes
	 */
	public IPv4Address(byte[] bytes) {
		super();
		if (bytes.length < BYTESLENGTH) {
			throw new AddressFormatException("Array too short, len="
					+ bytes.length);
		}
		int data = 0;
		for (int i = 0; i < BYTESLENGTH; i++) {
			data = (data << 8) | (bytes[i] & 0xFF);
		}
		addressData = data;
	}
	
	/** Converts the network address in network byte order from an array to
	 * numbers-and-dots notation.
	 * 
	 * @param bytes IPv4 address in network byte order, byte array at least
	 * four bytes long
	 * 
	 * @return The string representation of the IP
	 * 
	 * @exception AddressFormatException
	 */
	public static String stringFormat(byte[] bytes) {
		if (bytes.length < BYTESLENGTH) {
			throw new AddressFormatException("Array too short, len="
					+ bytes.length);
		}
		int value = 0;
		for(int i = 0; i < BYTESLENGTH; i++) {
			value = (value << 8) | (bytes[i] & 0xFF);
		}
		return stringFormat(value);
	}
	
	/** Converts the network address in network byte order from an int to
	 * numbers-and-dots notation.
	 * @param address IPv4 address in network byte order
	 * @return String
	 */
	public static String stringFormat(int address) {
		
		return ((address >> 24) & 0xFF) + "." + ((address >> 16) & 0xFF) + "."
				+ ((address >> 8) & 0xFF) + "." + (address & 0xFF);
	}
	
	static int stringParse(String ipString) {
		if (!InternetAddressMatcher.matches(ipString)) {
			throw new AddressFormatException(ipString);
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

	@Override
	public String toString() {
		return stringFormat(addressData);
	}
	
	/**
	 * @return four bytes long array representing an IPv4 address in network
	 * by order
	 */
	@Override
	public byte[] toBytes() {
		byte[] answer = new byte[BYTESLENGTH];
		
		answer[0] = (byte)((addressData >> 24) & 0xFF);
		answer[1] = (byte)((addressData >> 16) & 0xFF);
		answer[2] = (byte)((addressData >> 8) & 0xFF);
		answer[3] = (byte)(addressData & 0xFF);

		return answer;
	}
	
	/**
	 * @return four bytes long array representing an IPv4 address in network
	 * by order
	 */
	public int toInteger() {
		return addressData;
	}

	private long toLong() {
		return addressData & 0xFFFFFFFFL;
	}
	
	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if (!(obj instanceof IPv4Address)){
			return false;
		}
		return (addressData == ((IPv4Address)obj).addressData);
	}
	
	@Override
	public int hashCode(){
		int n = 0;
		for(byte b : toBytes()) {
			n <<= 4;
			n ^= (b & 0xFF);
		}
		return n;
	}
	
	// FIXME // james
	//public IPv6Address toIPv6Address() {
	//	return new IPv6Address(this);
	//}

	@Override
	public boolean isUnspecified() {
		return addressData == 0;
	}
	
	// RFC3171 + RFC1112
	@Override
	public boolean isMultiCast() {
		return toLong() >> 28 == 0xe;
	}
	
	@Override
	public boolean isLoopback() {
		return toLong() >> 24 == 127;
	}
	
	@Override
	public boolean isPrivate() {
		int b0 = (addressData >> 24) & 0xFF;
		int b1 = (addressData >> 16) & 0xFF;
		if (b0 == 10) return true;
		if (b0 == 172) {
			if (b1 >= 16 && b1 < 32) return true;
			return false;
		}
		if (b0 == 192 && b1 == 168) return true;
		return false;
	}

	public boolean isReserved() {
		return toLong() >> 27 == 0x1e;
	}
	
	/* Automatic Private IP Addressing (APIPA) */
	@Override
	public boolean isLinkLocal() {
		int b0 = (addressData >> 24) & 0xFF;
		int b1 = (addressData >> 16) & 0xFF;
		return b0 == 169 && b1 == 254;
	}
	
	public int compareTo(INetworkAddress other) {
		if (!(other instanceof InternetAddress)) {
			return -1;
		}
		IPv4Address other4;
		if (other instanceof IPv6Address) {
			IPv6Address other6 = (IPv6Address)other;
			if (other6.isV4Mapped() || other6.isV4Compatible()) {
				other4 = other6.toIPv4Address();
			} else {
				return -1;
			}
		} else {
			other4 = (IPv4Address) other;
		}
		long anotherVal = other4.toLong();
		return toLong() < anotherVal ? -1 : (toLong() == anotherVal ? 0 : 1);
	}
	
	@Override
	public IPv4Netblock createNetblock(int maskBitCount) {
		return new IPv4Netblock(this, maskBitCount);
	}
	
	/**
	 * @return an IPv4Address
	 * 
	 * @exception AddressFormatException if not IPv4 (or IPv6-mapped/compatible)
	 */
	public static IPv4Address fromInetAddress(final InetAddress address) {
		InternetAddress addr = fromBytes(address.getAddress());
		if (addr instanceof IPv6Address) {
			return ((IPv6Address)addr).toIPv4Address();
		}
		return (IPv4Address) addr;
	}
	
	/**
	 * @return an IPv4Address
	 * 
	 * @exception AddressFormatException if not IPv4 (or IPv6-mapped/compatible)
	 */
	public static IPv4Address fromString(final String address) {
		InternetAddress addr = InternetAddress.fromString(address);
		if (addr instanceof IPv6Address) {
			return ((IPv6Address)addr).toIPv4Address();
		}
		return (IPv4Address) addr;
	}
}
