package com.netifera.platform.net.http.internal.service;

import com.netifera.platform.net.http.service.HTTP;
import com.netifera.platform.net.services.INetworkService;
import com.netifera.platform.net.services.INetworkServiceProvider;
import com.netifera.platform.util.locators.ISocketLocator;

public class HTTPProvider implements INetworkServiceProvider {

	public INetworkService create(ISocketLocator locator) {
		return new HTTP(locator);
	}

	public Class<? extends INetworkService> getServiceClass() {
		return HTTP.class;
	}

	public String getServiceName() {
		return "HTTP";
	}
}
