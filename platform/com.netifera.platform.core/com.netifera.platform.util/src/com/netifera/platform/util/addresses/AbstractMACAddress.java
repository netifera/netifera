package com.netifera.platform.util.addresses;

import java.io.Serializable;
import java.util.Arrays;

/*
 * a MAC address is not formally a Network address, it is an Hardware address.
 *
 * see http://standards.ieee.org/regauth/oui/tutorials/UseOfEUI.html
 */
/**
 * Media Access Control address (MAC address) or Ethernet Hardware Address (EHA)
 */
abstract class AbstractMACAddress implements IHardwareAddress,
		Serializable {
	private static final long serialVersionUID = -6362575938645715808L;
	
	protected static final int OUI_SIZE = 3;
	protected final byte[] bytes;
	private final int length; /* java bytes */
	
	protected AbstractMACAddress(final int length, final byte[] bytes) {
		assert length > OUI_SIZE : "Invalid MAC format";
		if (bytes.length != length) {
			throw new AddressFormatException("Invalid MAC address length:" +
					bytes.length);
		}
		this.length = length;
		this.bytes = bytes.clone();
	}
	
	protected AbstractMACAddress(final int length, final String address) {
		this.length = length;
		this.bytes = parseString(address);
	}
	
	public int getDataSize() {
		return length * 8;
	}
	
	private byte[] parseString(String address) {
		String[] parts = null;
		if (address.contains(":")) {
			parts = address.split(":");
		} else if (address.contains("-")) {
			parts = address.split("-");
		} else if (address.contains(".") && address.startsWith("0x")) {
			String[] p = address.substring(2).split("\\.");
			if (p.length != length/2) {
				throw new AddressFormatException("Invalid MAC address format:"
						+ address);
			}
			parts = new String[length];
			for (int i = 0; i < length/2; i++) {
				int len = p[i].length();
				if (len > 2) {
					parts[2 * i] = p[i].substring(0, len - 2);
					parts[(2 * i) + 1] = p[i].substring(2);
				} else {
					parts[2 * i] = "00";
					parts[(2 * i) + 1] = p[i];
				}
			}
		}
		if (parts == null || parts.length != length) {
			throw new AddressFormatException("Invalid MAC address format:" +
					address);
		}
		byte[] bytes = new byte[length];
		for (int i=0; i<length; i++) {
			bytes[i] = (byte) Integer.parseInt(parts[i], 16);
		}
		return bytes;
	}
	
	public byte[] toBytes() {
		return bytes.clone();
	}
	
	/**
	 * The Organizationally Unique Identifier
	 */
	public byte[] getOUI() {
		byte[] oui = new byte[OUI_SIZE];
		System.arraycopy(bytes, 0, oui, 0, OUI_SIZE);
		return oui;
	}
	
	public byte[] getNIC() {
		byte[] nic = new byte[length - OUI_SIZE];
		System.arraycopy(bytes, OUI_SIZE, nic, 0, length - OUI_SIZE);
		return nic;
	}
	
	public boolean isUnicast() {
		return (bytes[0] & 1) == 0;
	}
	
	public boolean isMulticast() {
		return (bytes[0] & 1) != 0;
	}
	
	public boolean isBroadcast() {
		for (byte x: bytes) {
			if ((x & 0xFF) != 255) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isGloballyUnique() {
		return (bytes[0] & 2) == 0;
	}
	
	public boolean isLocallyAdministered() {
		return (bytes[0] & 2) != 0;
	}
	
	@Override
	public int hashCode() {
		int answer = 0;
		for (byte x: bytes) {
			answer = answer*256 + (x & 0xFF);
		}
		return answer;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof AbstractMACAddress)) {
			return false;
		}
		return Arrays.equals(bytes, ((AbstractMACAddress)obj).toBytes());
	}
		
	@Override
	public String toString() {
		return bytesToString(length, bytes);
	}
	
	protected static String bytesToString(int length, final byte[] bytes) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < length; i++) {
			if (i > 0) {
				buffer.append(':');
			}
			buffer.append(String.format("%02x", Integer.valueOf(bytes[i] & 0xff)));
		}
		return buffer.toString();
	}
}
