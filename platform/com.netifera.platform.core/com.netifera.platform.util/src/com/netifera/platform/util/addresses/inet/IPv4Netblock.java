package com.netifera.platform.util.addresses.inet;

import java.util.NoSuchElementException;

import com.netifera.platform.util.addresses.AddressFormatException;

public class IPv4Netblock extends InternetNetblock {
	
	private static final long serialVersionUID = -1490064632240429900L;
	
	private static final int MAX_MASKBIT = IPv4Address.BYTESLENGTH * 8;;
	private static long[] subnetMaskIpSize;

	static {
		subnetMaskIpSize = new long[MAX_MASKBIT + 1];
		int ipNumber = 1;
		for (int i = MAX_MASKBIT; i != 0; i--) {
			subnetMaskIpSize[i] = ipNumber;
			ipNumber <<= 1;
		}
	}
	
	public IPv4Netblock(final IPv4Address network, final int maskBitCount) {
		super(network, maskBitCount);
	}
	
	public IPv4Netblock(final byte[] bytes, final int maskBitCount) {
		super(IPv4Address.BYTESLENGTH, bytes, maskBitCount);
	}
	
	//public IPv4Netblock(final String fromIP, final String toIP) {
	//	super(fromIP, toIP);
	//}
	
	@Override
	protected IPv4Address netblockStartAddress(InternetAddress network,
			int maskBitCount) {
		return new IPv4Address(((IPv4Address)network).addressData
				& makeMask(maskBitCount));
	}
	
	@Override
	public IPv4Address getNetworkAddress() {
		return (IPv4Address) network;
	}
	
	@Override
	public IPv4Address getNetmaskAddress() {
		return new IPv4Address(makeMask(maskBitCount));
	}
	
	@Override
	public IPv4Address getBroadcastAddress() {
		return new IPv4Address(getNetworkAddress().addressData
				| ~makeMask(maskBitCount));
	}
	
	/**
	 * @return the smaller network Mask (in bits) required to fit ipNumber
	 * InternetAddress.
	 */
	public static int getMaskBits(final int ipNumber) {
		if (ipNumber < 0) {
			throw new
				IllegalArgumentException("Invalid IPv4 number: " + ipNumber);
		}
		int maskBit = MAX_MASKBIT;
		while(maskBit != 0 && subnetMaskIpSize[maskBit] < ipNumber) {
			maskBit--;
		}
		return maskBit;
	}
	
	public static long getIpNumber(int maskBit) {
		if (maskBit < 0 || maskBit > MAX_MASKBIT) {
			throw new
				IllegalArgumentException("Invalid IPv4 bitmask: " + maskBit);
		}
		return subnetMaskIpSize[maskBit];
	}
	
	public boolean contains(final InternetAddress address) {
		if(!(address instanceof IPv4Address)) { // FIXME about encapsulable?
			return false;
		}
		
		IPv4Address ipv4 = (IPv4Address) address;
		
		int mask = makeMask();
		
		return ((ipv4.addressData & mask)
				== (getNetworkAddress().addressData & mask));
	}
	
	private int makeMask() {
		return makeMask(maskBitCount);
	}
	
	public static int makeMask(final int maskBitCount) {
		return maskBitCount == 0 ? 0 : (1 << 31) >> (maskBitCount - 1);
	}
	
	public boolean isIndexedIterable() {
		return maskBitCount >= 2; /* to fit java signed integer */
	}
	
	public IPv4Address itemAt(final int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("index:" + index
					+ " for itemcount:" + itemCount());
		}
		if (!isIndexedIterable()) {
			throw new NoSuchElementException("index:" + index);
		}
		return new IPv4Address(getNetworkAddress().addressData & makeMask()
				| index);
	}
	
	/**
	 * @return how many InternetAddress this Netblock contains.
	 * @see com.netifera.platform.util.addresses.INetworkblock#itemCount()
	 */
	public int itemCount() {
		if (!isIndexedIterable()) {
			return 0;
		}
		return (int) subnetMaskIpSize[maskBitCount];
		// return (1 << (MAX_MASKBIT - maskBitCount));
	}
	
	/**
	 * Hosts available for this network block.
	 * 
	 * @return the number of hosts available
	 */
	public int availableHosts() {
		int r = itemCount() - 2;
		if (r <= 0) {
			/* RFC 3021 for PPP links */
			r += 2;
		}
		return r;
	}
	
	/*
	 * see RCF 3330: Special-Use IPv4 Addresses
	 * see IANA Internet Protocol v4 Address Space
	 */

	public boolean isBogus() {
		byte[] bytes = network.toBytes();
		int b0 = bytes[0] & 0xFF;
		int b1 = bytes[1] & 0xFF;
		int b2 = bytes[2] & 0xFF;
		
		return (b0 == 192 && b1 == 0 && b2 == 2 && maskBitCount >= 24); // 192.0.2/24
	}
	   
	/* TODO:
	 * - 0/7 2/8 5/8
	 * - 27/8 31/8 36/7 39/8
	 * - 42/8 46/8 49/8 50/8 100/5 108/6 112/7
	 * - 173/8 174/7 176/6
	 * - 193/8 223/8 241->255/8
	 */
	public boolean isReserved() {
		return (network.toBytes()[0] & 0xFF) == 240 && maskBitCount >= 4; // 240/4
	}
	
	@Override
	public boolean isLoopback() {
		return (network.toBytes()[0] & 0xFF) == 127 && maskBitCount >= 8; // 127/8
	}

	public boolean isLocal() {
		byte[] bytes = network.toBytes();
		int b0 = bytes[0] & 0xFF;
		int b1 = bytes[1] & 0xFF;
		
		return (b0 == 169 && b1 == 254 && maskBitCount >= 16) // 169.254/16
				|| isLoopback();
	}
	
	public boolean isPrivate() {
		/*
		 * see RFC 1918: Address Allocation for Private Internets
		 */
		byte[] bytes = network.toBytes();
		int b0 = bytes[0] & 0xFF;
		if (b0 == 10) return maskBitCount >= 8; // 10/8
		int b1 = bytes[1] & 0xFF;
		if (b0 == 172) {
			return b1 >= 16 && b1 <= 32 && maskBitCount >= 12; // 172.16/12
		}
		if (b0 == 192 && b1 == 168) return maskBitCount >= 16; // 192.168/16
		return false;
	}

	public int compareTo(final IPv4Netblock other) {
		int r;
		r = network.compareTo(other.network);
		if (r > 0) {
			return 1;
		} else if (r < 0) {
			return -1;
		}
		return maskBitCount < other.maskBitCount ? 1
				: (maskBitCount == other.maskBitCount ? 0 : -1);
	}

	public int compareTo(InternetNetblock other) {
		if (other instanceof IPv4Netblock) {
			return compareTo((IPv4Netblock)other);
		}
		return -1; // XXX
	}
	
	/*
	 * see RFC 1112: Host Extensions for IP Multicasting
	 * see IANA Internet Multicast Addresses
	 */
	// TODO isMulticast()
	
	/**
	 * @param addr IP address in network byte order
	 * @param maskBitCount bit mask
	 * 
	 * @exception IllegalArgumentException if the prefix is invalid.
 	 * @exception AddressFormatException if the array length is not valid
	 */
	public static IPv4Netblock fromData(byte[] addr, int maskBitCount) {
		verifyIPv4Buffer(addr);
		return (IPv4Netblock) IPv4Address.fromBytes(addr).createNetblock(
				maskBitCount);
	}
	
	private static void verifyIPv4Buffer(byte[] addr) {
		if (addr.length != IPv4Address.BYTESLENGTH) {
			throw new AddressFormatException("Invalid IPv4 array size: "
					+ addr.length);
		}
	}
}
