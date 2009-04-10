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

public class OpenWrtWebManager extends WebApplication implements IAuthenticable {
	private static final long serialVersionUID = 6031896891074472398L;

	public OpenWrtWebManager(HTTP http, URI url) {
		super(http, url);
	}

	public CredentialsVerifier createCredentialsVerifier() {
		URI url = getURL().resolve("/cgi-bin/webif/system-settings.sh");
		return new HttpBasicAuthenticationVerifier(getHTTP(), url);
	}

	public List<Credential> defaultCredentials() {
		ArrayList<Credential> answer = new ArrayList<Credential>();
		answer.add(new UsernameAndPassword("","admin"));
		answer.add(new UsernameAndPassword("","test"));
		return answer;
	}

	public boolean isAuthenticableWith(Credential credential) {
		return credential instanceof UsernameAndPassword;
	}
}
