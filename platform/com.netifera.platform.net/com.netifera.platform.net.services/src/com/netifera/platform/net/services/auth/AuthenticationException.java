package com.netifera.platform.net.services.auth;

import java.io.IOException;

public class AuthenticationException extends IOException {
	private static final long serialVersionUID = 4549351652580211778L;

	public AuthenticationException(String message) {
		super(message);
	}
}
