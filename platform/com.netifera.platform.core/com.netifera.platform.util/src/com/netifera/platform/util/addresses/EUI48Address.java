package com.netifera.platform.util.addresses;

public final class EUI48Address extends AbstractMACAddress
		implements Comparable<EUI48Address> {
	private static final long serialVersionUID = 75053307876854740L;

	public static final int MACLENGTH = 6;

	public EUI48Address(byte[] bytes) {
		super(MACLENGTH, bytes);
	}
	
	public EUI48Address(String address) {
		super(MACLENGTH, address);
	}
	
	public static String bytesToString(final byte[] bytes) {
		return AbstractMACAddress.bytesToString(MACLENGTH, bytes);
	}
	
	public int compareTo(final EUI48Address anotherAddress) {
		int val;
		for (int i = 0; i < MACLENGTH; i++) {
			val = (bytes[i] & 0xff) - (anotherAddress.bytes[i] & 0xff);
			if (val != 0) {
				return val;
			}
		}
		return 0;
	}
	
	public EUI48Address newInstance() {
		return new EUI48Address(bytes);
	}
	
	public EUI64Address toEUI64EncapsulatedAddress() {
		byte[] eui = new byte[EUI64Address.MACLENGTH];
		System.arraycopy(bytes, 0, eui, 0, OUI_SIZE);
		eui[OUI_SIZE] = (byte) 0xff;
		eui[OUI_SIZE + 1] = (byte) 0xfe;
		System.arraycopy(bytes, OUI_SIZE, eui, OUI_SIZE + 2,
				MACLENGTH - OUI_SIZE);
		return new EUI64Address(eui);
	}
}
