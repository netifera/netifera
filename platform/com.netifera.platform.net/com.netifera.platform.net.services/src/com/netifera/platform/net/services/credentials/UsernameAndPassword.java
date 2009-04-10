package com.netifera.platform.net.services.credentials;


public class UsernameAndPassword implements Credential {
	private static final long serialVersionUID = 2458896370499029332L;

	private final String username;
	private final String password;
	
	public UsernameAndPassword(final String username, final String password) {
		this.username = username;
		this.password = password;
	}

	public UsernameAndPassword(final String username, final Password password) {
		this(username, password.getPasswordString());
	}
	
	public String getUsernameString() {
		return username;
	}
	
	public String getPasswordString() {
		return password;
	}
	
	public Password getPassword() {
		return new Password(password);
	}
	
	@Override
	public String toString() {
		return username+":"+password;
	}
}
