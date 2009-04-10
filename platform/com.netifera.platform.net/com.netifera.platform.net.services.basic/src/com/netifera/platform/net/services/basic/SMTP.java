package com.netifera.platform.net.services.basic;

import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.smtp.SMTPClient;

import com.netifera.platform.net.services.NetworkService;
import com.netifera.platform.util.locators.ISocketLocator;
import com.netifera.platform.util.locators.TCPSocketLocator;


public class SMTP extends NetworkService {
	private static final long serialVersionUID = 6595378589137067267L;

	public SMTP(ISocketLocator locator) {
		super(locator);
	}
	
	public TCPSocketLocator getLocator() {
		return (TCPSocketLocator) super.getLocator();
	}
	
	public SMTPClient createClient() throws SocketException, IOException {
		SMTPClient client = new SMTPClient();
		client.connect(getLocator().getAddress().toInetAddress(), getLocator().getPort());
		return client;
	}
	
	@Override
	public String getURIScheme() {
		return isSSL() ? "smtps" : "smtp";
	}
	
	public int getDefaultPort() {
		return isSSL() ? 465 : 25;
	}
}
