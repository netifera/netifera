package com.netifera.platform.net.internal.services.basic;

import com.netifera.platform.net.services.INetworkService;
import com.netifera.platform.net.services.INetworkServiceProvider;
import com.netifera.platform.net.services.basic.FTP;
import com.netifera.platform.util.locators.ISocketLocator;

public class FTPProvider implements INetworkServiceProvider {

	public Class<? extends INetworkService> getServiceClass() {
		return FTP.class;
	}

	public String getServiceName() {
		return "FTP";
	}

	public FTP create(ISocketLocator locator) {
		return new FTP(locator);
	}
}
