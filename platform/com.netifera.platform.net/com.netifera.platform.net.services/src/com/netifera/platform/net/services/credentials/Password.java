package com.netifera.platform.net.services.credentials;


public class Password implements Credential {
	private static final long serialVersionUID = 3092967094774827308L;

	private final String password;

	public Password(final String password) {
		this.password = password;
	}
	
	public String getPasswordString() {
		return password;
	}
	
	@Override
	public String toString() {
		return password;
	}
}
