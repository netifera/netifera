package com.netifera.platform.internal.model.sync;

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
import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.internal.model.Workspace;
import com.netifera.platform.model.RequestModelUpdate;
import com.netifera.platform.model.SetModelIdPrefix;

public class ModelSynchronizer {
	
	private ILogger logger;
	private IModelService model;
	private IMessageDispatcher dispatcher;
	private Map<IMessenger, ChannelSynchronizer> channelSynchronizers 
		= new HashMap<IMessenger, ChannelSynchronizer>();
	
	private void requestUpdateHandler(IMessenger messenger, RequestModelUpdate msg) {
		logger.debug("Model updates requested, start updates at index " + msg.getStartingIndex());
		if(channelSynchronizers.containsKey(messenger)) {
			respondError(messenger, msg, "Updates already being sent on this channel");
			return;
		}
		
		final ChannelSynchronizer cs = new ChannelSynchronizer(this, messenger, logger, msg.getStartingIndex());
		channelSynchronizers.put(messenger, cs);
		respondOk(messenger, msg);
		cs.start();
		
	
	}
	
	private void setModelIdPrefixHandler(IMessenger messenger, SetModelIdPrefix msg) {
		logger.debug("Set model prefix " + msg.getPrefix());
		if(model.getCurrentWorkspace().setModelIdPrefix(msg.getPrefix(), false)) {
			respondOk(messenger, msg);
		} else {
			respondError(messenger, msg, "Failed to set prefix id to " + msg.getPrefix());
		} 
	}
	
	
	
	private void respondOk(IMessenger messenger, IProbeMessage msg) {
		try {
			messenger.respondOk(msg);
		} catch (MessengerException e) {
			// channel closed, just return
		}
	}
	private void respondError(IMessenger messenger, IProbeMessage msg, String error) {
		try {
			messenger.respondError(msg, error);
		} catch (MessengerException e) {
			// channel closed, just return
		}
	}
	private void registerHandlers(IMessageDispatcher dispatcher) {
		final IMessageHandler handler = new IMessageHandler() {

			public void call(IMessenger messenger, IProbeMessage message)
					throws DispatchException {
				if(message instanceof RequestModelUpdate) {
					requestUpdateHandler(messenger, (RequestModelUpdate)message);				
				} else if(message instanceof SetModelIdPrefix) {
					setModelIdPrefixHandler(messenger, (SetModelIdPrefix) message);
				} else {
					throw new DispatchMismatchException(message);
				}				
			}
			
		};
		
		dispatcher.registerMessageHandler(RequestModelUpdate.ID, handler);
		dispatcher.registerMessageHandler(SetModelIdPrefix.ID, handler);
	}

	synchronized void removeChannelSynchronizer(IMessenger messenger) {
		channelSynchronizers.remove(messenger);
	}
	
	Workspace getWorkspace() {
		return (Workspace)model.getCurrentWorkspace();
	}
	/* OSGi DS bind methods */
	protected void setDispatcher(IMessageDispatcherService dispatcherService) {
		dispatcher = dispatcherService.getServerDispatcher();
		registerHandlers(dispatcher);
	}
	
	protected void unsetDispatcher(IMessageDispatcherService dispatcherService) {
		
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Model Synchronizer");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}
	
	protected void setModelService(IModelService modelService) {
		this.model = modelService;
	}
	
	protected void unsetModelService(IModelService modelService) {
		
	}

}
