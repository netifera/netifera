package com.netifera.platform.net.http.web.applications.examples;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.net.http.service.HTTP;
import com.netifera.platform.net.http.service.auth.HttpBasicAuthenticationVerifier;
import com.netifera.platform.net.http.web.applications.WebApplication;
import com.netifera.platform.net.services.auth.CredentialsVerifier;
import com.netifera.platform.net.services.auth.IAuthenticable;
import com.netifera.platform.net.services.credentials.Credential;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;

public class TomcatAdmin extends WebApplication implements IAuthenticable {
	private static final long serialVersionUID = -3389423781033426455L;

	public TomcatAdmin(HTTP http, URI url) {
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
		URI url = getURL().resolve("login.jsp");
		return new HttpBasicAuthenticationVerifier(getHTTP(), url);
	}
}
