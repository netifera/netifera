package com.netifera.platform.internal.dispatcher;

import org.osgi.service.component.ComponentContext;

import com.netifera.platform.api.dispatcher.IClientDispatcher;
import com.netifera.platform.api.dispatcher.IMessageDispatcherService;
import com.netifera.platform.api.dispatcher.IServerDispatcher;
import com.netifera.platform.api.log.ILogManager;

public class MessageDispatcherService implements IMessageDispatcherService {
	
	private  ClientDispatcher client; 
	private  ServerDispatcher server; 
	
	private ILogManager logManager;
	
	public IClientDispatcher getClientDispatcher() {
		return client;
	}

	public IServerDispatcher getServerDispatcher() {
		return server;
	}
	
	protected void activate(ComponentContext ctx) {
		createDispatchers();
	}
	
	private void createDispatchers() {
		server = new ServerDispatcher(logManager);
		client = new ClientDispatcher(server, logManager);
	}
	
	protected void deactivate(ComponentContext ctx) {
		client = null;
		server = null;
	}
	
	protected void setLogManager(ILogManager manager) {
		logManager = manager;
	}
	
	protected void unsetLogManager(ILogManager manager) {
		logManager = null;
	}

}
