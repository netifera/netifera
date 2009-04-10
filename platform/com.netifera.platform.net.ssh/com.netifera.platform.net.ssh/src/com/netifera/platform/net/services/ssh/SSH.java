package com.netifera.platform.net.services.ssh;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.netifera.platform.net.services.NetworkService;
import com.netifera.platform.net.services.auth.AuthenticationException;
import com.netifera.platform.net.services.auth.IAuthenticable;
import com.netifera.platform.net.services.credentials.Credential;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;
import com.netifera.platform.util.locators.ISocketLocator;
import com.netifera.platform.util.locators.TCPSocketLocator;
import com.trilead.ssh2.Connection;


public class SSH extends NetworkService implements IAuthenticable {
	private static final long serialVersionUID = -401876893019505594L;

	public SSH(ISocketLocator locator) {
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
	
	@Override
	public String getURIScheme() {
		return "ssh";
	}

	@Override
	public int getDefaultPort() {
		return 22;
	}
	
	public Connection createConnection() throws IOException {
		Connection conn = new Connection(getLocator().getAddress().toString(), getLocator().getPort());
		conn.connect();
		return conn;
	}

	public Connection createConnection(Credential credential) throws IOException {
		if (credential instanceof UsernameAndPassword) {
			return createConnection((UsernameAndPassword) credential);
		} else if (credential instanceof SSHKey) {
			return createConnection((SSHKey) credential);
		}
		return null;
	}
	
	public Connection createConnection(UsernameAndPassword credential) throws IOException, AuthenticationException {
		Connection conn = createConnection();
		if (!conn.authenticateWithPassword(credential.getUsernameString(), credential.getPasswordString())) {
			conn.close();
			throw new AuthenticationException("Bad username or password");
		}
		return conn;
	}

	public Connection createConnection(SSHKey credential) throws IOException, AuthenticationException {
		Connection conn = createConnection();
		if (!conn.authenticateWithPublicKey(credential.getUsernameString(), credential.getKeyData(), credential.getPasswordString())) {
			conn.close();
			throw new AuthenticationException("Bad key");
		}
		return conn;
	}
}
