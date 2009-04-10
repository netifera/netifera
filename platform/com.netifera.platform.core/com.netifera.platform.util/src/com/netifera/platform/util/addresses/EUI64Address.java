package com.netifera.platform.util.addresses;

/*
 * The EUI-64 value was originally conceived as a mechanism to avoid excess
 * consumption of OUIvalues within high-volume non-networking applications.
 */
public final class EUI64Address extends AbstractMACAddress
		implements Comparable<EUI64Address> {
	private static final long serialVersionUID = -4590898788343842527L;

	public static final int MACLENGTH = 8;

	public EUI64Address(byte[] bytes) {
		super(MACLENGTH, bytes);
	}
	
	public EUI64Address(String address) {
		super(MACLENGTH, address);
	}

	public static String bytesToString(final byte[] bytes) {
		return AbstractMACAddress.bytesToString(MACLENGTH, bytes);
	}
	
	public int compareTo(final EUI64Address anotherAddress) {
		int val;
		for (int i = 0; i < MACLENGTH; i++) {
			val = (bytes[i] & 0xff) - (anotherAddress.bytes[i] & 0xff);
			if (val != 0) {
				return val;
			}
		}
		return 0;
	}
	
	public EUI64Address newInstance() {
		return new EUI64Address(bytes);
	}
	
	public boolean isEUI48Encapsulated() {
		return (bytes[OUI_SIZE] & 0xff) == 0xff
			&& (bytes[OUI_SIZE + 1] & 0xff) == 0xfe;
	}
	
	public boolean isMAC48Encapsulated() {
		return (bytes[OUI_SIZE] & 0xff) == 0xff
			&& (bytes[OUI_SIZE + 1] & 0xff) == 0xff;
	}
	
	public EUI48Address toEUI48Address() {
		return new  EUI48Address(data48());
	}
	
	public MAC48Address toMAC48Address() {
		return new  MAC48Address(data48());
	}
	
	private byte[] data48() {
		byte[] data = new byte[EUI48Address.MACLENGTH];
		
		System.arraycopy(bytes, 0, data, 0, OUI_SIZE);
		System.arraycopy(bytes, OUI_SIZE + 2, data, OUI_SIZE,
				MACLENGTH - OUI_SIZE - 2);
		
		return data;
	}
	
	@Override
	public String toString() {
		if (isEUI48Encapsulated()) {
			return super.toString() + " (EUI48 Encapsulated)";
		}
		if (isMAC48Encapsulated()) {
			return super.toString() + " (MAC Encapsulated)";
		}
		return super.toString();
	}
}
