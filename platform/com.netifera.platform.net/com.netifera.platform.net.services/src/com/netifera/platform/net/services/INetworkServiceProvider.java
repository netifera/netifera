package com.netifera.platform.net.services;

import com.netifera.platform.util.locators.ISocketLocator;


public interface INetworkServiceProvider {
	String getServiceName();
	Class<? extends INetworkService> getServiceClass();
	INetworkService create(ISocketLocator locator);
}
