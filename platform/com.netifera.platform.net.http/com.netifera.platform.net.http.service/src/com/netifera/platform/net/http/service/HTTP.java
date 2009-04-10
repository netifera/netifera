package com.netifera.platform.net.http.service;

import org.apache.http.nio.protocol.HttpRequestExecutionHandler;
import org.apache.http.nio.reactor.IOReactorException;

import com.netifera.platform.net.services.NetworkService;
import com.netifera.platform.util.locators.ISocketLocator;
import com.netifera.platform.util.locators.TCPSocketLocator;

public class HTTP extends NetworkService {
	private static final long serialVersionUID = -4369719970659667081L;

	public HTTP(ISocketLocator locator) {
		super(locator);
	}

	@Override
	public TCPSocketLocator getLocator() {
		return (TCPSocketLocator) super.getLocator();
	}
	
	public HTTPClient createClient() {
		return new HTTPClient(getLocator());
	}
	
	public AsynchronousHTTPClient createAsynchronousClient(HttpRequestExecutionHandler requestHandler) throws IOReactorException {
		return new AsynchronousHTTPClient(getLocator(), null, requestHandler);
	}
	
	@Override
	public String getURIScheme() {
		return isSSL() ? "https" : "http";
	}
	
	public int getDefaultPort() {
		return isSSL() ? 443 : 80;
	}
}
