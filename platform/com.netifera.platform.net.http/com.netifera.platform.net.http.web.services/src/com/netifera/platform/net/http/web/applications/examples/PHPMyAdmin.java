package com.netifera.platform.net.http.web.applications.examples;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.net.http.service.HTTP;
import com.netifera.platform.net.http.web.applications.WebApplication;
import com.netifera.platform.net.services.auth.CredentialsVerifier;
import com.netifera.platform.net.services.auth.IAuthenticable;
import com.netifera.platform.net.services.credentials.Credential;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;

public class PHPMyAdmin extends WebApplication implements IAuthenticable {
	private static final long serialVersionUID = -3011313740258448937L;

	public PHPMyAdmin(HTTP http, URI url) {
		super(http, url);
	}

	public List<Credential> defaultCredentials() {
		List<Credential> answer = new ArrayList<Credential>();
		answer.add(new UsernameAndPassword("admin", "admin"));
		return answer;
	}

	public boolean isAuthenticableWith(Credential credential) {
		return credential instanceof UsernameAndPassword;
	}

	public CredentialsVerifier createCredentialsVerifier() {
		URI url = getURL().resolve("index.php");
//		return new HttpFormAuthenticationVerifier(http, "username", "password", url);
		return null;
	}
}
