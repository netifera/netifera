package com.netifera.platform.util.addresses;

/*
 * classes implementing that interface are recommended to also implement:
 * 
 * - public static implementationAddress fromBytes(final byte[] bytes);
 *
 * - public static implementationAddress fromString(final String address);
 */
public interface IAbstractAddress {
	
	/**
	 * The size (in bits) required to store an address.
	 * 
	 * @return the size (in bits) required to store an address.
	 */
	int getDataSize();
	
	/**
	 * @return the address in binary form
	 */
	byte[] toBytes();
	
	/**
	 * @return string representation of the address
	 */
	String toString();
}
