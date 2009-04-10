package com.netifera.platform.util.addresses;

/*
 * The distinction between EUI-48 and MAC-48 identifiers is purely semantic:
 * MAC-48 is used for network hardware; EUI-48 is used to identify other devices
 * and software. (Thus, by definition, an EUI-48 is not in fact a "MAC address",
 * although it is syntactically indistinguishable from one and assigned from the
 * same numbering space.)
 */
public /*final*/ class MAC48Address extends AbstractMACAddress
		implements Comparable<MAC48Address> {
	private static final long serialVersionUID = 512830668584924625L;

	public static final int MACLENGTH = 6;

	public static final MAC48Address ANY =
		new MAC48Address("00:00:00:00:00:00");
	
	// null identifier
	public static final MAC48Address BROADCAST =
		new MAC48Address("ff:ff:ff:ff:ff:ff");
	
	public MAC48Address(byte[] bytes) {
		super(MACLENGTH, bytes);
	}
	
	public MAC48Address(String address) {
		super(MACLENGTH, address);
	}
	
	public static String bytesToString(final byte[] bytes) {
		return AbstractMACAddress.bytesToString(MACLENGTH, bytes);
	}
	
	public int compareTo(final MAC48Address anotherAddress) {
		int val;
		for (int i = 0; i < MACLENGTH; i++) {
			val = (bytes[i] & 0xff) - (anotherAddress.bytes[i] & 0xff);
			if (val != 0) {
				return val;
			}
		}
		return 0;
	}
	
	public MAC48Address newInstance() {
		return new MAC48Address(bytes);
	}
	
	public EUI64Address toEUI64EncapsulatedAddress() {
		byte[] eui = new byte[EUI64Address.MACLENGTH];
		System.arraycopy(bytes, 0, eui, 0, OUI_SIZE);
		eui[OUI_SIZE] = (byte) 0xff;
		eui[OUI_SIZE + 1] = (byte) 0xff;
		System.arraycopy(bytes, OUI_SIZE, eui, OUI_SIZE + 2,
				MACLENGTH - OUI_SIZE);
		return new EUI64Address(eui);
	}
}
