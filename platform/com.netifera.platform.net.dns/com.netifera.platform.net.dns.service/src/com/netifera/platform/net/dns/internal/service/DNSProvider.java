package com.netifera.platform.net.dns.internal.service;

import com.netifera.platform.net.dns.service.DNS;
import com.netifera.platform.net.services.INetworkService;
import com.netifera.platform.net.services.INetworkServiceProvider;
import com.netifera.platform.util.locators.ISocketLocator;

public class DNSProvider implements INetworkServiceProvider {
	
	public Class<? extends INetworkService> getServiceClass() {
		return DNS.class;
	}

	public String getServiceName() {
		return "DNS";
	}

	public DNS create(ISocketLocator locator) {
		return new DNS(locator);
	}
}
