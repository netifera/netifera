package com.netifera.platform.internal.dispatcher.channels;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.ComponentContext;

import com.netifera.platform.api.channels.IChannelConnecter;
import com.netifera.platform.api.channels.IChannelFactory;
import com.netifera.platform.api.channels.IChannelRegistry;
import com.netifera.platform.api.channels.IChannelServer;
import com.netifera.platform.api.dispatcher.IMessageDispatcherService;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.internal.dispatcher.DispatcherActivator;

public class ChannelRegistry implements IChannelRegistry {
	private final static String CHANNEL_PROPERTY_PREFIX = "com.netifera.channel.";
	private final Map<String, IChannelFactory> channelFactories = new HashMap<String, IChannelFactory>();

	/* only the ones that have been created by the registry itself with properties */
	private final Set<IChannelServer> activeServers = new HashSet<IChannelServer>();
	
	private IMessageDispatcherService dispatcherService;
	private ILogger logger;
	private volatile boolean isActivated;
	
	public boolean isChannelRegistered(String channelType) {
		return channelFactories.containsKey(channelType);
	}
	
	public IChannelConnecter createConnecter(String config) {
		final String channelType = configToChannelType(config);
		if(channelType == null) {
			logger.error("Illegal channel configuration description: " + config);
			return null;
		}
		
		if(channelType.equals("local")) {
			return new LocalChannelConnecter(dispatcherService.getClientDispatcher());
		}
		return getFactory(channelType).createConnecter(dispatcherService.getClientDispatcher(), config);
	}
	
	private String configToChannelType(String channelConfig) {
		final String parts[] = channelConfig.split(":");
		if(parts.length < 1) {
			return null;
		} else {
			return parts[0];
		}
	}
	
	public IChannelServer createServer(String config) {
		final String channelType = configToChannelType(config);
		return getFactory(channelType).createServer(dispatcherService.getServerDispatcher(), config);
	}
	
	private IChannelFactory getFactory(String channelType) {
		if(!channelFactories.containsKey(channelType)) {
			throw new IllegalArgumentException("Request for unregistered channel type: " + channelType);
		}
		return channelFactories.get(channelType);
	}
	
	
	protected void activate(ComponentContext ctx) {
		logger.enableDebug();
		logger.debug("Activating");
		synchronized (channelFactories) {
			isActivated = true;
			startChannelsFromProperties();
			
		}
		
	}
	
	protected void deactivate(ComponentContext ctx) {
		// XXX stop channels here?
	}
	
	private void startChannelsFromProperties() {
		for(String type : channelFactories.keySet()) {
			createChannelFromProperty(type, getProperty(type));
		}
	}
	
	private String getProperty(String key) {
		return DispatcherActivator.getInstance().getProperty(CHANNEL_PROPERTY_PREFIX + key);
	}
	
	private void createChannelFromProperty(String type, String configData) {
		if(configData == null) 
			return;
		logger.debug("Creating channel with data " + configData);
		final IChannelFactory factory = channelFactories.get(type);
		assert(factory != null);
		
		final IChannelServer server = factory.createServer(dispatcherService.getServerDispatcher(), configData);
		if(server == null) {
			logger.warning("Failed to create channel server of type '" + type + "' from configuration string: " + configData);
			return;
		}
		if(!startChannelServer(server)) {
			logger.warning("Failed to start channel server of type '" + type + "' created from configuration string: " + configData);
			return;
		}
		logger.info("Channel server of type '" + type + "' started");
		activeServers.add(server);
	}
	
	private boolean startChannelServer(IChannelServer server) {
		try {
			server.startListening();
			return true;
		} catch(IOException e) {
			logger.error("IO error attempting to start server channel", e);
			return false;
		}
	}
	protected void registerChannelFactory(IChannelFactory factory) {
		synchronized (channelFactories) {
			channelFactories.put(factory.getType(), factory);
			if(isActivated)
				createChannelFromProperty(factory.getType(), getProperty(factory.getType()));

		}
	}
	
	protected void unregisterChannelFactory(IChannelFactory factory) {
		synchronized (channelFactories) {
			channelFactories.remove(factory.getType());
		}
	}
	
	protected void setDispatcher(IMessageDispatcherService dispatcherService) {
		this.dispatcherService = dispatcherService;
	}
	
	protected void unsetDispatcher(IMessageDispatcherService dispatcherService) {
		this.dispatcherService = null;
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Channel Registry");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		logger = null;
	}
}
