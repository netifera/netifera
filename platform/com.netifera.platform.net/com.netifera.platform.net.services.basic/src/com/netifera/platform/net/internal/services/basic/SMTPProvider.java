package com.netifera.platform.net.internal.services.basic;

import com.netifera.platform.net.services.INetworkService;
import com.netifera.platform.net.services.INetworkServiceProvider;
import com.netifera.platform.net.services.basic.SMTP;
import com.netifera.platform.util.locators.ISocketLocator;

public class SMTPProvider implements INetworkServiceProvider {

	public Class<? extends INetworkService> getServiceClass() {
		return SMTP.class;
	}

	public String getServiceName() {
		return "SMTP";
	}

	public SMTP create(ISocketLocator locator) {
		return new SMTP(locator);
	}
}
