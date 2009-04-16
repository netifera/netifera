package com.netifera.platform.internal.channel.tcplisten;

import java.net.InetSocketAddress;

import com.netifera.platform.api.channels.IChannelConnecter;
import com.netifera.platform.api.channels.IChannelFactory;
import com.netifera.platform.api.channels.IChannelServer;
import com.netifera.platform.api.dispatcher.IClientDispatcher;
import com.netifera.platform.api.dispatcher.IServerDispatcher;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.channel.socket.ConfigParser;
import com.netifera.platform.channel.tcplisten.ITCPListenServer;

public class TCPListenChannelFactory implements IChannelFactory {
	private final ConfigParser configParser = new ConfigParser(ITCPListenServer.CHANNEL_TYPE);
	private ILogger logger;
	public IChannelConnecter createConnecter(IClientDispatcher clientDispatcher, String channelConfig) {
		final InetSocketAddress address = configParser.configToAddress(channelConfig);
		
		/* XXX should throw a checked exception instead of returning null? */
		if(address == null) 
			return null;
		
		final TCPListenConnecter connecter = new TCPListenConnecter(clientDispatcher);
		connecter.setAddress(address);
		return connecter;
	}

	
	public IChannelServer createServer(IServerDispatcher serverDispatcher, String config) {
		final int listenPort = configParser.stringToPort(config);
		if(listenPort != ConfigParser.INVALID_PORT) {
			return createConfiguredServer(new InetSocketAddress(listenPort), serverDispatcher);
		}
		
		final InetSocketAddress listenAddress = configParser.configToAddress(config);
		if(listenAddress == null)
			return null;
		
		return createConfiguredServer(listenAddress, serverDispatcher);
		
	}
	
	
	
	private IChannelServer createConfiguredServer(InetSocketAddress listenAddress, IServerDispatcher serverDispatcher) {
		final TCPListenChannelServer server = new TCPListenChannelServer(serverDispatcher, logger);
		server.setListenAddress(listenAddress);
		return server;
	}
	public String getType() {
		return ITCPListenServer.CHANNEL_TYPE;
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("TCP Listen Channel");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		logger = null;
	}


}
