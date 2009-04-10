package com.netifera.platform.tools.internal;

import java.util.HashMap;
import java.util.Map;

import com.netifera.platform.api.dispatcher.DispatchException;
import com.netifera.platform.api.dispatcher.DispatchMismatchException;
import com.netifera.platform.api.dispatcher.IMessageDispatcher;
import com.netifera.platform.api.dispatcher.IMessageDispatcherService;
import com.netifera.platform.api.dispatcher.IMessageHandler;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.tasks.ITask;
import com.netifera.platform.api.tasks.ITaskManagerService;
import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolProvider;
import com.netifera.platform.tools.ToolLaunchMessage;

public class ToolLauncher {
	
	private Map<String, IToolProvider> providerMap = new HashMap<String, IToolProvider>();
	private ITaskManagerService taskManager;
	private ILogger logger;
	
	/* ToolLaunchMessage  handler  */
	
	private void toolLaunchHandler(IMessenger messenger, ToolLaunchMessage message) {
		try {
			processAndRespond(messenger, message);
		} catch(MessengerException e) {
			logger.warning("Exception sending toolLaunch response: " + e.getMessage(), e);
		}
	}
	
	private void processAndRespond(IMessenger messenger, ToolLaunchMessage message) throws MessengerException {
		try {
			final ITask task = createToolAndTask(messenger, message);
			messenger.emitMessage(message.createResponse(task.getTaskId()));
		} catch(Exception e) {
			messenger.respondError(message, "Tool execution failed: " + e.getMessage());
		}
	}
	
	private ITask createToolAndTask(IMessenger messenger, ToolLaunchMessage message) {
		final ITool tool = createToolInstance(message.getClassName());
		final ITask task = createTaskForTool(messenger, message, tool);
		return task;
	}
	
	private ITool createToolInstance(String className) {
		if(!providerMap.containsKey(className)) {
			logger.error("Could not find tool provider for " + className);
			throw new RuntimeException("No tool provider found");
		}
		
		final ITool tool = providerMap.get(className).createToolInstance(className);
		if(tool == null) {
			logger.error("Failed to create tool instance for " + className);
			throw new RuntimeException("Tool creation failed.");
		}
		return tool;
	}
	
	private ITask createTaskForTool(IMessenger messenger, ToolLaunchMessage message, ITool tool) {
		final ToolContext ctx = new ToolContext(tool, message.getConfiguration(), message.getSpaceId());
		return taskManager.createTask(ctx, messenger);
	}
	
	/*
	 * Register ToolLaunchMessage --> toolLaunchHandler
	 */
	
	private void registerHandlers(IMessageDispatcher dispatcher) {
		IMessageHandler handler = new IMessageHandler() {
			public void call(IMessenger messenger, IProbeMessage message)
					throws DispatchException {
				if(message instanceof ToolLaunchMessage) {
					toolLaunchHandler(messenger, (ToolLaunchMessage)message);
				} else {
					throw new DispatchMismatchException(message);
				}
			}
		};
		
		dispatcher.registerMessageHandler(ToolLaunchMessage.ID, handler);
	}

	/* 
	 * OSGi DS 
	 */
	
	protected void setTaskManagerService(ITaskManagerService taskManager) {
		this.taskManager = taskManager;
	}

	protected void unsetTaskManagerService(ITaskManagerService taskManager) {
		
	}

	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Tool Launcher");
		logger.enableDebug();
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		logger = null;
	}

	protected void setDispatcher(IMessageDispatcherService dispatcherService) {
		registerHandlers(dispatcherService.getServerDispatcher());
	}

	protected void unsetDispatcher(IMessageDispatcherService dispatcherService) {
		
	}

	protected void registerToolProvider(IToolProvider toolProvider) {
	
		for(String name : toolProvider.getProvidedToolClassNames()) {
			if(providerMap.containsKey(name)) {
				logger.warning("Tool provider is trying to register duplicate class: " + name);
			} else {
				providerMap.put(name, toolProvider);
			}
		}
	}

	protected void unregisterToolProvider(IToolProvider toolProvider) {
		for(String className : toolProvider.getProvidedToolClassNames()) {
			if(providerMap.remove(className) == null) {
				logger.warning("Trying to remove unregistered tool class: " + className);
			}
		}
		
	}

}
