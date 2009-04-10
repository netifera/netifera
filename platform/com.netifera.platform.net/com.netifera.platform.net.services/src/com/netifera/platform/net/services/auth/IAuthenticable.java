package com.netifera.platform.net.services.auth;

import java.io.Serializable;
import java.util.List;

import com.netifera.platform.net.services.credentials.Credential;

public interface IAuthenticable extends Serializable {
	public boolean isAuthenticableWith(Credential credential);
	public List<Credential> defaultCredentials();
//	public CredentialsVerifier createCredentialsVerifier();
//	Credential getCredential();
}
