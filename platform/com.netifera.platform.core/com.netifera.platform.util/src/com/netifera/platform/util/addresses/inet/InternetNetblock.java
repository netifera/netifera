package com.netifera.platform.util.addresses.inet;

import java.io.Serializable;
import java.util.Iterator;

import com.netifera.platform.api.iterables.IndexedIterable;
import com.netifera.platform.api.iterables.SequentialIterator;
import com.netifera.platform.util.addresses.AddressFormatException;
import com.netifera.platform.util.addresses.INetblock;

/**
 * CIDR blocks
 * 
 * @see com.netifera.platform.util.addresses.inet.IPv4Netblock
 * @see com.netifera.platform.util.addresses.inet.IPv6Netblock
 */
public abstract class InternetNetblock implements INetblock<InternetAddress>,
		Comparable<InternetNetblock>, IndexedIterable<InternetAddress>,
		Serializable {
		
	private static final long serialVersionUID = 8850929139528536587L;
	
	protected final InternetAddress network;
	
	/**
	 * The prefix length, the number of shared initial bits, counting from the
	 * left-hand side of the address.
	 */
	protected final int maskBitCount;
	
	/**
	 * The 'base' address of that network block.
	 */
	public abstract InternetAddress getNetworkAddress();
	
	/**
	 * A broadcast address is an IP address that allows information to be sent
	 * to all machines on a given subnet rather than a specific machine. 
	 */
	public abstract InternetAddress getBroadcastAddress();

	/**
	 * A subnet mask is a bitmask that encodes the prefix length in a form
	 * similar to an IP address
	 */
	public abstract InternetAddress getNetmaskAddress();
	
	protected InternetNetblock(int size, byte[] bytes, int maskBitCount) {
		if (size != bytes.length) {
			throw new AddressFormatException("Bad address size: " + size);
		}
		InternetAddress addr = InternetAddress.fromBytes(bytes);
		if (!addr.isValidMaskBit(maskBitCount)) {
			throw new IllegalArgumentException("Invalid maskBit: "
					+ maskBitCount);
		}
		this.maskBitCount = maskBitCount;
		this.network = netblockStartAddress(addr, maskBitCount);
	}
	
	protected InternetNetblock(InternetAddress network, int maskBitCount) {
		if (!network.isValidMaskBit(maskBitCount)) {
			throw new IllegalArgumentException("Invalid maskBit: "
					+ maskBitCount);
		}
		this.maskBitCount = maskBitCount;
		this.network = netblockStartAddress(network, maskBitCount);
	}
	
	protected abstract InternetAddress netblockStartAddress(
			InternetAddress network, int maskBitCount);
	
	/**
	 * Creates a new instance of this object.
	 * 
	 * @return A new InternetAddress instance
	 */
	public InternetNetblock newInstance() {
		return network.createNetblock(maskBitCount);
	}
	
	/**
	 * The number of shared initial bits, counting from the left-hand side of
	 * the address.
	 * 
	 * @return the network Mask (in bits) of this network block
	 */
	public int getCIDR() {
		return maskBitCount;
	}
	
	/**
	 * @param address An IP address
	 * @param maskBitCount bit mask
	 * 
	 * @exception IllegalArgumentException if the prefix is invalid.
	 */
	public static InternetNetblock fromAddress(InternetAddress address,
			int maskBitCount) {
		return address.createNetblock(maskBitCount);
	}
	
	/**
	 * @param netblock An network block in CIDR notation (address '/' mask)
	 * 
	 * @exception IllegalArgumentException
 	 * @exception AddressFormatException
	 */
	public static InternetNetblock fromString(String netblock) {
		String[] arg = netblock.split("/"); // TODO '-' separator
		if (arg.length != 2) {
			throw new IllegalArgumentException(
					"Invalid Netblock (Missing '/' separator)");
		}
		// TODO short notation: 193/8, 193.165.64/19
		try {
			return fromString(arg[0], Integer.parseInt(arg[1]));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid Netblock (wrong CIDR)",
					e);
		}
	}
	
	/**
	 * @param address An IP address
	 * @param maskBitCount bit mask
	 * 
	 * @exception IllegalArgumentException if the prefix is invalid.
 	 * @exception AddressFormatException
	 */
	public static InternetNetblock fromString(String address, int maskBitCount) {
		return InternetAddress.fromString(address).createNetblock(maskBitCount);
	}
	
	/**
	 * @param addr IP address in network byte order
	 * @param maskBitCount bit mask
	 * 
	 * @exception IllegalArgumentException if the prefix is invalid.
 	 * @exception AddressFormatException if the array length is not valid
	 */
	public static InternetNetblock fromData(byte[] addr, int maskBitCount) {
		return InternetAddress.fromBytes(addr).createNetblock(maskBitCount);
	}
	
	public static InternetNetblock fromRange(String fromIP, String toIP) {
		InternetAddress from = InternetAddress.fromString(fromIP);
		InternetAddress to = InternetAddress.fromString(toIP);
		if (from.getNetworkFamily().compareTo(to.getNetworkFamily()) != 0) {
			// FIXME encapsulated?
			throw new IllegalArgumentException("Different families: from/to");
		}
		if (from instanceof IPv6Address) { // TODO
			throw new UnsupportedOperationException("IPv6 from-to netblock");
		}
		int maskBitCount = IPv4Netblock.getMaskBits(((IPv4Address)to)
				.addressData - ((IPv4Address)from).addressData);
		return fromAddress(from, maskBitCount);
	}
	
	/**
	 * @param arpa network address ARPA notation.
	 * 
	 * @see InternetAddress#fromARPA(String)
	 */
	public static InternetNetblock fromARPA(String arpa) {
		String[] members = arpa.split("\\.");
		if (members.length < 3) {
			throw new AddressFormatException("Bad address format: "	+ arpa);
		}
		StringBuffer sb = new StringBuffer(IPv6Address.MAX_TEXTUAL_LENGTH);
		int maskBits;
		if (arpa.endsWith(".in-addr.arpa.")) {
			// TODO handles RFC2317 (Classless delegation) ?
			
			for (int i = members.length - 3, j = 0; j < 4; i--, j++) {
				if (i >= 0) {
					sb.append(members[i]);
				} else {
					sb.append('0');	
				}
				if (j + 1 != 4) {
					sb.append('.');
				}
			}
			maskBits = (members.length - 2) * 8;
		} else if (arpa.endsWith(".ip6.arpa.")) {
			for (int i = members.length - 3, j = 0; j < 32; i--, j++) {
				if (i >= 0) {
					sb.append(members[i]);
				} else {
					sb.append('0');	
				}
				if (j % 4 == 3 && j + 1 != 32) {
					sb.append(':');
				}
			}
			maskBits = (members.length - 2) * 4;
		} else {
			throw new AddressFormatException("Bad address format: "	+ arpa);
		}
		return fromString(sb.toString(), maskBits);
	}
	
	@Override
	public String toString() {
		return network.toString() + "/" + maskBitCount;
	}
	
	@Override
	public int hashCode() {
		return network == null ? 0 : network.hashCode() >>> maskBitCount;
	}

	@Override
	public boolean equals(final Object obj) {
		if(!(obj instanceof InternetNetblock)){
			return false;
		}
		
		InternetNetblock other = (InternetNetblock) obj;
		if (maskBitCount != other.maskBitCount){
			return false;
		}
		return network.equals(other.network);
	}
	
	public abstract boolean isLoopback();
	
	// TODO
	
	//public abstract boolean isLocal();
	
	//public abstract boolean isPrivate();
	
	//public abstract boolean isMulticast();
	
	public IndexedIterable<InternetAddress> getIndexedIterable() {
		if (!isIndexedIterable()) {
			return null;
		}
		return this;
	}
	
	public Iterator<InternetAddress> iterator() {
		return new SequentialIterator<InternetAddress>(this);
	}
}
