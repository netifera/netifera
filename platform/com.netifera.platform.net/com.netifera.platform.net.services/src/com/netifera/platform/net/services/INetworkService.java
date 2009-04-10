package com.netifera.platform.net.services;

import java.io.Serializable;

import com.netifera.platform.util.locators.ISocketLocator;

public interface INetworkService extends Serializable {
	ISocketLocator getLocator();
}
