package com.netifera.platform.util.addresses.inet;

import java.util.Arrays;

/*
 * An IPv6 subnet always has a /64 prefix which provides 64 bits for the host
 * portion of an address. Although it is technically possible to use smaller
 * subnets, they are impractical for local area networks because stateless
 * address autoconfiguration of network interfaces (RFC 4862) requires a /64
 * address.
 */
public class IPv6Netblock extends InternetNetblock {
	
	private static final long serialVersionUID = -3749021380156916044L;
	
	private static final int MAX_MASKBIT = IPv6Address.BYTESLENGTH * 8;;
	
	public IPv6Netblock(IPv6Address address, int maskBitCount) {
		this(address.toBytes(), maskBitCount);
	}

	public IPv6Netblock(byte[] bytes, int maskBitCount) {
		super(IPv6Address.BYTESLENGTH, bytes, maskBitCount);
	}
	
	@Override
	public IPv6Address getNetworkAddress() {
		return (IPv6Address) network;
	}

	private byte makeMask(int maskBitCount) {
		return (byte) (0xff << (8 - maskBitCount));
	}
	
	@Override
	public IPv6Address getNetmaskAddress() {
		byte[] data = new byte[IPv6Address.BYTESLENGTH];
		int s = maskBitCount / 8;
		Arrays.fill(data, 0, s, (byte) 0xff);
		if (maskBitCount < MAX_MASKBIT) {
			data[s] = makeMask(maskBitCount - (s * 8));
		}
		return new IPv6Address(data);
	}
	
	@Override
	public IPv6Address getBroadcastAddress() {
		if (maskBitCount == MAX_MASKBIT) {
			return getNetworkAddress();
		}
		byte[] data = getNetworkAddress().toBytes();
		int s = maskBitCount / 8;
		if (s < 15) {
			Arrays.fill(data, s + 1, IPv6Address.BYTESLENGTH, (byte) 0xff);
		}
		data[s] |= ~makeMask(maskBitCount - (s * 8));
		
		return new IPv6Address(data);
	}
	
	@Override
	protected IPv6Address netblockStartAddress(InternetAddress addr,
				int maskBitCount) {
		if (maskBitCount == MAX_MASKBIT) {
			return (IPv6Address) addr.newInstance(); // FIXME newInstance() ?
		}
		byte[] ndata = new byte[IPv6Address.BYTESLENGTH];
		byte[] odata = addr.toBytes();
		int s = maskBitCount / 8;
		System.arraycopy(addr.toBytes(), 0, ndata, 0, s);
		ndata[s] = (byte) (odata[s] & makeMask(maskBitCount - (s * 8)));
		
		return new IPv6Address(ndata);
	}
	
	public int compareTo(IPv6Netblock anotherNetblock) {
		int r;
		r = network.compareTo(anotherNetblock.network);
		if (r > 0) {
			return 1;
		} else if (r < 0) {
			return -1;
		}
		return maskBitCount < anotherNetblock.maskBitCount ? 1
				: (maskBitCount == anotherNetblock.maskBitCount ? 0 : -1);
	}

	public int compareTo(InternetNetblock other) {
		if (other instanceof IPv6Netblock) {
			return compareTo((IPv6Netblock)other);
		}
		return -1; // XXX
	}
	
	public boolean contains(InternetAddress address) {
		IPv6Address addr;
		if (address instanceof IPv6Address) {
			addr = (IPv6Address) address;
		} else {
			addr = new IPv6Address((IPv4Address) address); // TODO verify
		}
		return Arrays.equals(network.toBytes(),
				netblockStartAddress(addr, maskBitCount).toBytes());
	}
	
	@Override
	public boolean isLoopback() {
		return maskBitCount == MAX_MASKBIT && getNetworkAddress().isLoopback();
	}
	
	/* a IPv6/96 is equivalent in size to the whole IPv4 address space */
	public boolean isIndexedIterable() {
		return maskBitCount >= 98; /* to fit java signed integer */
	}
	
	public InternetAddress itemAt(int index) {
		byte[] data = new byte[IPv6Address.BYTESLENGTH];
		System.arraycopy(getNetworkAddress().toBytes(), 0, data, 0, 12);
		for (int i = 0; i < 4; i++) {
			data[12 + i] = (byte) ((index >> (8 * (3 - i))) & 0xff);
		}
		return new IPv6Address(data);
	}

	public int itemCount() {
		if (!isIndexedIterable()) {
			return 0;
		}
		return (1 << (MAX_MASKBIT - maskBitCount));
	}
}
