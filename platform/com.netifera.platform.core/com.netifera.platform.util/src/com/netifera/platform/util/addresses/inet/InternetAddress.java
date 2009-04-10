package com.netifera.platform.util.addresses.inet;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.netifera.platform.util.addresses.AddressFormatException;
import com.netifera.platform.util.addresses.INetworkAddress;
import com.netifera.platform.util.addresses.NetworkFamily;

/**
 * This class represents an Internet Protocol (IP) address.
 */
public abstract class InternetAddress implements INetworkAddress, Serializable {
	
	private static final long serialVersionUID = -1585463461457645087L;
	
	/**
	 * Textual representation of the address family.
	 * 
	 * @return The textual representation of the address family
	 */
	public String familyName() {
		return getNetworkFamily().toString();
	}
	
	/**
	 * Creates a new instance of this object.
	 * 
	 * @return A new InternetAddress instance
	 */
	public InternetAddress newInstance() {
		return fromBytes(toBytes());
	}
	
	/**
     * Returns an <code>InternetAddress</code> object given the raw IP address. 
     * 
	 * @param bytes IP address in network byte order
	 *  
 	 * @exception AddressFormatException if the array length is not valid
	 */
	public static InternetAddress fromBytes(final byte[] bytes) {
		switch (bytes.length) {
		case IPv4Address.BYTESLENGTH:
			return new IPv4Address(bytes);
		case IPv6Address.BYTESLENGTH:
			return new IPv6Address(bytes);
		default:
			throw new AddressFormatException("Bad address size: "
					+ bytes.length);
		}
	}
	
	/**
     * Returns an <code>InternetAddress</code> object given the string.
     * 
	 * @param address the specified IP in decimal notation
	 * 
 	 * @exception AddressFormatException
	 */
	public static InternetAddress fromString(final String address) {
		if (address.contains(":")) {
			return new IPv6Address(address);
		} else {
			return new IPv4Address(address);
		}
	}
	
	/**
     * Returns an <code>InternetAddress</code> given an InetAddress object.
     * 
	 * @param address the specified IP
	 */
	public static InternetAddress fromInetAddress(final InetAddress addr) {
		return fromBytes(addr.getAddress());
	}
	
	/**
	 * Returns an <code>InternetAddress</code> object given a string in ARPA
	 * reverse notation.
	 * 
	 * The in-addr.arpa notation is one way of representing the IP address,
	 * useful for the practical business of performing lookups.
	 * 
	 * @param arpa address in ARPA notation.
	 */
	public static InternetAddress fromARPA(String arpa) {
		if (!arpa.matches("^.*\\.arpa\\.?$")) {
			throw new AddressFormatException("Bad address format: "	+ arpa);
		}
		String[] members = arpa.split("\\.");
		String addressString;
		switch (members.length) {
		case 4 + 2: // IPv4 (in-addr.arpa)
			addressString = members[3] + '.' + members[2]
			    + '.' + members[1] + '.' + members[0];
			break;
		case 32 + 2: // IPv6 (ip6.arpa)
			StringBuffer sb = new StringBuffer(IPv6Address.MAX_TEXTUAL_LENGTH);
			for (int i = 7; i >= 0; i--) {
				for (int j = 3; j >= 0; j--) {
					sb.append(members[4 * i + j]);
				}
				if (i > 0) {
					sb.append(':');
				}
			}
			addressString = sb.toString();
			break;
		default:
			throw new AddressFormatException("Bad address format: "	+ arpa);
		}
		return fromString(addressString);
	}
	
    /**
     * Converts this IP address to a <code>InetAddress</code>.
     */
	public InetAddress toInetAddress() {
		try {
			return InetAddress.getByAddress(toBytes()); // safe InetAddress call
		} catch (UnknownHostException e) {
			/* never happens */
			return null;
		}
	}
	
	/**
	 * Byte array representing the IP address in network by order.
	 * 
	 * @return A byte array representing the IP address in network by order
	 */
	public abstract byte[] toBytes();
	
	/**
	 * Numeric representation of the IP address.
	 * 
	 * @return A string representing the IP address
	 */
	@Override
	public abstract String toString();
	
	/**
	 * Literal IP Address Format in URL's Syntax (RFC 2732).
	 * 
	 * @return A string representing the IP address suitable for URL use.
	 */
	public String toStringLiteral() {
		if (getNetworkFamily() == NetworkFamily.AF_INET6) {
			return '[' + toString() + ']';
		}
		return toString();
	}
	
	/**
	 * Create a network block address containing this address.
	 * 
	 * @param maskBitCount The netblock prefix length
	 * 
	 * @exception IllegalArgumentException if the prefix is invalid.
	 */
	public abstract InternetNetblock createNetblock(int maskBitCount);
	
	/**
	 * Test if the netblock prefix length is valid for that type of addresses.
	 * 
	 * @param maskBitCount The netblock prefix length to test
	 * 
     * @return a <code>boolean</code> indicating if the prefix length is valid;
     * or false otherwise.
	 */
	public boolean isValidMaskBit(final int maskBitCount) {
		return maskBitCount >= 0 && maskBitCount <= getDataSize();
	}
	
	/**
	 * Utility routine to check if the address is an unicast address.
	 *
	 * @return a <code>boolean</code> indicating if the address is an unicast
	 * address; or false otherwise.
	 */
	// FIXME should we consider IPv4 240/8 as Unicast?
	public boolean isUniCast() {
		return !isMultiCast();
	}
	
	/**
	 * The unspecified address is used only to indicate the absence of an
	 * address.
	 * 
	 * <p>The unspecified address is typically used as a source address for
	 * packets that are attempting to verify the uniqueness of a tentative
	 * address.</p>
	 * <p>The unspecified address is never assigned to an interface or used as a
	 * destination address.</p>
	 */
	public abstract boolean isUnspecified();
	
	/**
	 * Utility routine to check if the address is a multicast address.
	 *
	 * @return a <code>boolean</code> indicating if the address is a multicast
	 * address; or false otherwise.
	 */
	public abstract boolean isMultiCast();
	
	/**
	 * Link-local addresses are used by nodes when communicating with
	 * neighboring nodes on the same link.
	 * 
	 * <p>The scope of a link-local address is the local link.</p>
	 */
	public abstract boolean isLinkLocal();
	
	/**
	 * Utility routine to check if the address is a loopback address.
	 * 
	 * @return a <code>boolean</code> indicating if the address is
	 * a loopback address; or false otherwise.
	 */
	public abstract boolean isLoopback();
	
	/**
	 * Addresses for private or internal networks.
	 * 
	 * <p>None of the private address prefixes may be routed in the public
	 * Internet.</p>
	 */
	public abstract boolean isPrivate();
}
