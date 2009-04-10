package com.netifera.platform.net.internal.services.basic;

import com.netifera.platform.net.services.INetworkService;
import com.netifera.platform.net.services.INetworkServiceProvider;
import com.netifera.platform.net.services.basic.POP3;
import com.netifera.platform.util.locators.ISocketLocator;

public class POP3Provider implements INetworkServiceProvider {

	public Class<? extends INetworkService> getServiceClass() {
		return POP3.class;
	}

	public String getServiceName() {
		return "POP3";
	}

	public POP3 create(ISocketLocator locator) {
		return new POP3(locator);
	}
}
