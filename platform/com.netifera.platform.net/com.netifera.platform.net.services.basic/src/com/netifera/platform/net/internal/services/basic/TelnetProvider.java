package com.netifera.platform.net.internal.services.basic;

import com.netifera.platform.net.services.INetworkService;
import com.netifera.platform.net.services.INetworkServiceProvider;
import com.netifera.platform.net.services.basic.Telnet;
import com.netifera.platform.util.locators.ISocketLocator;

public class TelnetProvider implements INetworkServiceProvider {

	public Class<? extends INetworkService> getServiceClass() {
		return Telnet.class;
	}

	public String getServiceName() {
		return "Telnet";
	}

	public Telnet create(ISocketLocator locator) {
		return new Telnet(locator);
	}
}
