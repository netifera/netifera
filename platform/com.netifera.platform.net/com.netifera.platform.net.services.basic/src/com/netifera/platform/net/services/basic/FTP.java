package com.netifera.platform.net.services.basic;

import java.io.IOException;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;

import com.netifera.platform.net.services.NetworkService;
import com.netifera.platform.net.services.auth.AuthenticationException;
import com.netifera.platform.net.services.auth.IAuthenticable;
import com.netifera.platform.net.services.credentials.Credential;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;
import com.netifera.platform.util.locators.ISocketLocator;
import com.netifera.platform.util.locators.TCPSocketLocator;


public class FTP extends NetworkService implements IAuthenticable {
	private static final long serialVersionUID = -1559740994317977398L;

	public FTP(ISocketLocator locator) {
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
	
	public FTPClient createClient() throws SocketException, IOException {
		FTPClient client = new FTPClient();
		client.connect(getLocator().getAddress().toInetAddress(), getLocator().getPort());
//		client.enterLocalPassiveMode();
		return client;
	}
	
	public FTPClient createClient(UsernameAndPassword credential) throws SocketException, IOException, AuthenticationException {
		FTPClient client = createClient();
		if (!client.login(credential.getUsernameString(), credential.getPasswordString())) {
			client.disconnect();
			throw new AuthenticationException("Bad username or password");
		}
		return client;
	}
	
	@Override
	public String getURIScheme() {
		return isSSL() ? "ftps" : "ftp";
	}
	
	public int getDefaultPort() {
		return isSSL() ? 990 : 21;
	}
}
