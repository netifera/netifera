package com.netifera.platform.util.addresses;

//@Deprecated // use MAC48Address
public final class MACAddress extends MAC48Address {
	private static final long serialVersionUID = -8890397506194328221L;

	public MACAddress(byte[] bytes) {
		super(bytes);
	}
	
	public MACAddress(String address) {
		super(address);
	}
}
