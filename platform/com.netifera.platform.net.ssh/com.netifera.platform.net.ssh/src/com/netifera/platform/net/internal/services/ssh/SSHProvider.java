package com.netifera.platform.net.internal.services.ssh;

import com.netifera.platform.net.services.INetworkService;
import com.netifera.platform.net.services.INetworkServiceProvider;
import com.netifera.platform.net.services.ssh.SSH;
import com.netifera.platform.util.locators.ISocketLocator;

public class SSHProvider implements INetworkServiceProvider {

	public Class<? extends INetworkService> getServiceClass() {
		return SSH.class;
	}

	public String getServiceName() {
		return "SSH";
	}

	public SSH create(ISocketLocator locator) {
		return new SSH(locator);
	}
}
