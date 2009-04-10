package com.netifera.platform.util.addresses.inet;

import com.netifera.platform.util.addresses.AddressFormatException;

@SuppressWarnings("serial")
@Deprecated
public class InternetAddressFormatException extends AddressFormatException {
	
	public InternetAddressFormatException() {
		super();
	}
	
	public InternetAddressFormatException(String s) {
		super(s);
	}
}
