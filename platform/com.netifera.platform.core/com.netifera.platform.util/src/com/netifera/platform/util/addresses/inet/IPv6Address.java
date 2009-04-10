package com.netifera.platform.util.addresses.inet;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Locale;

import com.netifera.platform.util.addresses.AddressFormatException;
import com.netifera.platform.util.addresses.EUI64Address;
import com.netifera.platform.util.addresses.INetworkAddress;
import com.netifera.platform.util.addresses.NetworkFamily;

/*
 * see RFC 2373: IP Version 6 Addressing Architecture
 */
public class IPv6Address extends InternetAddress {
	
	private static final long serialVersionUID = 3470405877439887455L;

	private final byte[] bytes;
	
	public final static int BYTESLENGTH = 16;
	
	/* 8 groups of 4 hexadecimal digits, each group separated by a colon ':' */
	public final static int MAX_TEXTUAL_LENGTH = 39; // 8 * 4 + 7
	
	public final static IPv6Address loopback;
	public final static IPv6Address any; // unspecified
	
	final public NetworkFamily getNetworkFamily() {
		return NetworkFamily.AF_INET6;
	}
	
	static {
		any = new IPv6Address(new byte[BYTESLENGTH]);
		byte[] bytes = new byte[BYTESLENGTH];
		bytes[15] = 1;
		loopback = new IPv6Address(bytes);
	}
	
	public int getDataSize() {
		return BYTESLENGTH * 8;
	}
	
	/**
	 * @param bytes IPv6 address in network byte order, the array must be at
	 * least 16 bytes long
	 * 
	 * @exception AddressFormatException if the array is less than 16 bytes
	 */
	public IPv6Address(final byte[] bytes) {
		super();
		if (bytes.length < BYTESLENGTH) {
			throw new AddressFormatException("Bad address size: "
					+ bytes.length);
		}
		this.bytes = new byte[BYTESLENGTH];
		System.arraycopy(bytes, 0, this.bytes, 0, BYTESLENGTH);
	}
	
	public IPv6Address(final IPv4Address ipv4) {
		super();
		if (ipv4.addressData == 0) {
			bytes = any.bytes;
			return;
		}
		bytes = bytesMapped(ipv4.toBytes());
	}
	
	/**
	 * @param ipString the IP address
	 * 
	 * @exception AddressFormatException
	 */
	public IPv6Address(final String ipString) {
		this(stringParse(ipString));
	}
	
	private static byte[] bytesAt(final byte[] bytes, final int offset,
			final int length) {
		byte[] result = new byte[length];
		System.arraycopy(bytes, offset, result, 0, length);
		return result;
	}
	
	private byte[] bytesMapped(final byte[] ipv4Bytes) {
		return fillMapped(new byte[BYTESLENGTH], ipv4Bytes);
	}
	
	private static byte[] fillMapped(byte[] ipv6Bytes, byte[] ipv4Bytes) {
		for (int i = 0; i < 10; i++) {
			ipv6Bytes[11] = 0;
		}
		ipv6Bytes[10] = (byte)0xff;
		ipv6Bytes[11] = (byte)0xff;
		System.arraycopy(ipv4Bytes, 0, ipv6Bytes, 12, 4);
		return ipv6Bytes;
	}
	
	private static byte[] stringParse(final String ipString) {
		int length = ipString.length();
		if (length < 2) {
			throw new AddressFormatException(ipString);
		}
		String string = ipString;
		
		// literal addresses
		if (ipString.charAt(0) == '[' && ipString.charAt(length - 1) == ']') {
			string = ipString.substring(1, length - 1);
		} else if (ipString.toLowerCase(Locale.ENGLISH).endsWith(".ipv6-literal.net")) {
			string = ipString.split("\\.")[0].replace('-', ':');
		}
		
		// any address
		if ("::".equals(string)) {
			return any.bytes;
		}
		
		// ipv4 mapped
		if (string.contains(".")) {
			int index = string.lastIndexOf(':');
			long ipv4Num = IPv4Address.stringParse(
					string.substring(index + 1)) & 0xFFFFFFFFL;
			string = string.substring(0, index)	+ String.format(
					":%04x:%04x", ipv4Num >> 16, ipv4Num & 0xffff);
		}
		String[] parts = string.split(":", -1);
		if (parts.length > BYTESLENGTH / 2) {
			throw new AddressFormatException(ipString);
		}
		
		// compressed notation
		if (string.contains("::")) {
			int index = string.indexOf("::");
			boolean doublecolon_at_end = index == length - 2;
			StringBuilder sb = new StringBuilder(MAX_TEXTUAL_LENGTH + 1);
			if (index == 0) { // first member compressed: add first '0'
				sb.append('0');
			} else {
				sb.append(string.substring(0, index));
			}
			int count = BYTESLENGTH / 2 - parts.length;
			if (doublecolon_at_end) { // add final ':0'
				count += 1;
			}
			for (int i = 0; i <= count; i++) {
				sb.append(":0");
			}
			if (!doublecolon_at_end) { // add rest after double colon 
				sb.append(string.substring(index + 1));
			}
			string = sb.toString();
		}
		parts = string.split(":");
		if (parts.length != BYTESLENGTH / 2) {
			throw new AddressFormatException(ipString);
		}
		
		byte[] bytes = new byte[BYTESLENGTH];
		for (int i = 0; i < BYTESLENGTH; i += 2) {
			int value = Integer.parseInt(parts[i / 2], 0x10);
			bytes[i] = (byte) (value >> 8);
			bytes[i + 1] = (byte) (value & 0xff);
		}
		
		return bytes;
	}
	
	/** Converts the network address in network byte order from an array to
	 * numbers-and-dots notation.
	 * 
	 * @param address IPv6 address in network byte order, byte array at least
	 * 16 bytes long
	 * 
	 * @return The string representation of the IP
	 * 
	 * @exception AddressFormatException
	 */
	public static String stringFormat(final byte[] bytes) {
		if (bytes.length < BYTESLENGTH) {
			throw new AddressFormatException("Array too short, len="
					+ bytes.length);
		}
		StringBuffer buffer = new StringBuffer();
		
		boolean v4mapped = isV4Mapped(bytes);
		if (v4mapped || isV4Compatible(bytes)) {
			buffer.append("::");
			if (v4mapped) {
				buffer.append("ffff:");
			}
			IPv4Address ipv4 = new IPv4Address(bytesAt(bytes, 12, 4));
			switch (ipv4.addressData) {
			case 0:
				break;
			case 1:
				buffer.append('1');
				break;
			default:
				buffer.append(ipv4.toString());
			}
		} else {
			// normalize "::"
			int offset = -1;
			int size = 0;
			
			// FIXME probably TOO expensive
			int j0 = 2;
			for (; j0 < BYTESLENGTH && bytes[j0] != 0; j0 += 2);
			for (int i = BYTESLENGTH - j0; offset == -1 && i >= 3; i -= 2) {
				for (int j = j0; offset == -1 && j <= BYTESLENGTH - i; j++) {
					if (Arrays.equals(bytesAt(any.bytes, 0, i),
							bytesAt(bytes, j, i))) {
						offset = j;
						size = i;
					}
				}
			}
			for (int i=0; i<BYTESLENGTH; i+=2) {
				if (i == offset) {
					if (offset + size == BYTESLENGTH) {
						buffer.append("::");
						break;
					}
					buffer.append(':');
					i += size - 2;
					continue;
				}
				if (i != 0) {
					buffer.append(':');
				}
				buffer.append(String.format("%x",
					Integer.valueOf(((bytes[i] & 0xff) << 8)
						+ (bytes[i + 1] & 0xff))));
			}
		}
		return buffer.toString();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj){
			return true;
		}
		if (!(obj instanceof IPv6Address)){
			return false;
		}
		return Arrays.equals(((IPv6Address)obj).bytes, bytes);
	}
	
	@Override
	public int hashCode(){
		return Arrays.hashCode(bytes);
	}
	
	public int compareTo(INetworkAddress other) {
		if (!(other instanceof InternetAddress)) {
			return -1;
		}
		if (other instanceof IPv4Address) {
			if (isV4Mapped() || isV4Compatible()) {
				return toIPv4Address().compareTo(other);
			}
			return 1;
		}
		IPv6Address address = (IPv6Address) other;
		int value;
		for (int i = 0; i < BYTESLENGTH; i++) {
			value = (bytes[i] & 0xff) - (address.bytes[i] & 0xff);
			if (value != 0) {
				return value; // TODO shift i // james
			}
		}
		return 0;
	}
	
	@Override
	public byte[] toBytes() {
		return bytes.clone();
	}
	
	/**
	 * @return an IPv4Address if Mapped or Compatible
	 * 
	 * @exception AddressFormatException if not IPv4 mapped/compatible
	 */
	public IPv4Address toIPv4Address() {
		if (isV4Compatible(bytes) || isV4Mapped(bytes)) {
			return new IPv4Address(bytesAt(bytes, 12, 4));
		}
		throw new AddressFormatException("Address is not mapped/compatible");
	}
	
	private int octet(final int index) {
		return (bytes[index] & 0xFF);
	}
	
	@Override
	public String toString() {
		return stringFormat(bytes);
	}
	
	@Override
	public boolean isUnspecified() {
		return Arrays.equals(any.bytes, bytes);
	}
	
	@Override
	public boolean isLoopback() {
		return Arrays.equals(loopback.bytes, bytes);
	}
	
	@Override
	public boolean isMultiCast() {
		return octet(0) == 0xff;
	}
	
	@Override
	public boolean isLinkLocal() {
		return (octet(0) == 0xfe) && (octet(1) & 0x80) != 0;
	}
	
	/* XXX RFC 3879 deprecates RFC 3513: 
	 * "this prefix MUST no longer be supported in new implementations."
	 * FEC0::/48 <- ok? FEC0::/10 <- bad
	 */
	/**
	 * Site-local addresses are equivalent to the IPv4 private address space.
	 * 
	 * <p>The scope of a site-local address is the site.</p>
	 */
	public boolean isSiteLocal() {
		return (octet(0) == 0xfe) && ((octet(1) & 0xc0) != 0);
	}
	
	/**
	 * Aggregatable global unicast addresses are equivalent to public IPv4
	 * addresses. They are globally routable and reachable on the IPv6 Internet.
	 * 
	 * Aggregatable global unicast addresses are also known as global addresses.
	 */
	public boolean isGlobalUnicast() {
		return octet(0) >> 5 == 1;
	}
	
	/* RFC 4193 */
	public boolean isUniqueLocal() {
		return (octet(0) & 0xfc) == 0xfc;
	}

	// TODO: Multicast Scope: (Node/Link/Site/Organization)-local | Global
	
	@Override
	public boolean isPrivate() {
		return isUniqueLocal()
			|| isSiteLocal()	// XXX address range specification abandoned
			|| isLinkLocal();	// generated automatically by the OS's IP layer
	}
	
	/**
	 * Utility routine to check if this IPv6 address is an IPv4 mapped address.
	 *
	 * <p>IPv4 mapped addresses constitute a special class of IPv6 addresses:
	 * This address type has its first 80 bits set to zero, the next 16 set
	 * to one.</p>
	 * 
	 * @return a <code>boolean</code> indicating if the address is an IPv4
	 * mapped address; or false otherwise.
	 */
	public boolean isV4Mapped() {
		return isV4Mapped(bytes);
	}
	
	private static boolean isV4Mapped(final byte[] bytes) {
		for (int i = 0; i < 10; i++) {
			if (bytes[i] != 0) {
				return false;
			}
		}
		return bytes[10] == (byte)0xff && bytes[11] == (byte)0xff;
	}
	
	/**
	 * Utility routine to check if this IPv6 address is an IPv4 compatible
	 * address.
	 * 
	 * <p>Such an IPv6 address has its first 96 bits set to zero.</p>
	 * 
	 * <p><i>This address type has been deprecated by RFC 4291.</i></p>
	 * 
	 * @return a <code>boolean</code> indicating if the address is an IPv4
	 * compatible address; or false otherwise.
	 */
	public boolean isV4Compatible() {
		return isV4Compatible(bytes);
	}
	
	private static boolean isV4Compatible(final byte[] bytes) {
		for (int i = 0; i < 12; i++) {
			if (bytes[i] != 0) {
				return false;
			}
		}
		return true;
	}
	
	/* Derived from the interface's built-in 48-bit IEEE 802 address. */
	@Deprecated	// TODO check prefix first, else return null // james
	public EUI64Address getInterfaceIdentifier() {
		byte[] data = new byte[EUI64Address.MACLENGTH];
		System.arraycopy(bytes, BYTESLENGTH - EUI64Address.MACLENGTH,
				data, 0, EUI64Address.MACLENGTH);
		return new EUI64Address(data);
	}
	
	@Override
	public IPv6Netblock createNetblock(int maskBitCount) {
		return new IPv6Netblock(this, maskBitCount);
	}
	
	/**
	 * @return an IPv6Address
	 */
	public static IPv6Address fromInetAddress(final InetAddress addr) {
		InternetAddress ipaddr = fromBytes(addr.getAddress());
		if (ipaddr instanceof IPv4Address) {
			return new IPv6Address((IPv4Address)ipaddr); // FIXME verify
		}
		return (IPv6Address) ipaddr;
	}
	
	/**
	 * @return an IPv6Address
	 */
	public static IPv6Address fromString(final String address) {
		InternetAddress addr = InternetAddress.fromString(address);
		if (addr instanceof IPv4Address) {
			return new IPv6Address((IPv4Address)addr);
		}
		return (IPv6Address) addr;
	}
}
