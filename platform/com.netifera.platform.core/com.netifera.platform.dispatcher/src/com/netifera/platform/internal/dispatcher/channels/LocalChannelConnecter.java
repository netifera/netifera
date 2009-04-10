package com.netifera.platform.internal.dispatcher.channels;

import com.netifera.platform.api.channels.IChannelConnectProgress;
import com.netifera.platform.api.channels.IChannelConnecter;
import com.netifera.platform.api.dispatcher.IClientDispatcher;
import com.netifera.platform.api.dispatcher.IMessenger;

public class LocalChannelConnecter implements IChannelConnecter {

	private final IMessenger localMessenger;
	public LocalChannelConnecter(IClientDispatcher dispatcher) {
		localMessenger = dispatcher.getLocalMessenger();
	}
	
	public void abortConnect() {	
	}

	public void connect(IChannelConnectProgress progress) {
		progress.connectCompleted(localMessenger);		
	}

}
