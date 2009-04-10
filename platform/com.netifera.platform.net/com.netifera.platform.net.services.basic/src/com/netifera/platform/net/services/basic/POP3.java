package com.netifera.platform.net.services.basic;

import java.util.Collections;
import java.util.List;

import com.netifera.platform.net.services.NetworkService;
import com.netifera.platform.net.services.auth.IAuthenticable;
import com.netifera.platform.net.services.credentials.Credential;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;
import com.netifera.platform.util.locators.ISocketLocator;
import com.netifera.platform.util.locators.TCPSocketLocator;


public class POP3 extends NetworkService implements IAuthenticable {
	private static final long serialVersionUID = 4185535541765381350L;

	public POP3(ISocketLocator locator) {
		super(locator);
	}
	
	public TCPSocketLocator getLocator() {
		return (TCPSocketLocator) super.getLocator();
	}

	public boolean isAuthenticableWith(Credential credential) {
		return credential instanceof UsernameAndPassword;
	}

	public List<Credential> defaultCredentials() {
		return Collections.emptyList();
	}
	
	/* RFC 2384 */
	@Override
	public String getURIScheme() {
		return "pop";
	}
	
	public int getDefaultPort() {
		return 110;
	}
	
	@Override
	protected String getURIAuthorityPrefix() {
		if (isSSL()) {
			return ";AUTH=ssl@";
		}
		return "";
	}
}
