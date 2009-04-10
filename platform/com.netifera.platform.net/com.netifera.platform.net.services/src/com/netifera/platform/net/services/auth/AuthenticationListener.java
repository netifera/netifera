package com.netifera.platform.net.services.auth;

import com.netifera.platform.net.services.credentials.Credential;

public interface AuthenticationListener {
	void authenticationSucceeded(Credential credential);
	void authenticationFailed(Credential credential);
	void authenticationError(Credential credential, Throwable e);
}
