package com.netifera.platform.util.addresses;

public class AddressFormatException extends RuntimeException {
	private static final long serialVersionUID = 7379368806818686566L;

	public AddressFormatException() {
		super();
	}
	
	public AddressFormatException(String message) {
		super(message);
	}
	
	public AddressFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
