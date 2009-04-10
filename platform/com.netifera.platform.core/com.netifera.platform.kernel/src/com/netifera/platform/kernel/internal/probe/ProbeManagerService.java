package com.netifera.platform.kernel.internal.probe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.service.component.ComponentContext;

import com.netifera.platform.api.channels.IChannelConnecter;
import com.netifera.platform.api.channels.IChannelRegistry;
import com.netifera.platform.api.dispatcher.DispatchException;
import com.netifera.platform.api.dispatcher.DispatchMismatchException;
import com.netifera.platform.api.dispatcher.IClientDispatcher;
import com.netifera.platform.api.dispatcher.IMessageDispatcher;
import com.netifera.platform.api.dispatcher.IMessageDispatcherService;
import com.netifera.platform.api.dispatcher.IMessageHandler;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.events.EventListenerManager;
import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IModelPredicate;
import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.api.tasks.ITaskClient;
import com.netifera.platform.api.tasks.ITaskOutput;
import com.netifera.platform.api.tasks.ITaskStatus;
import com.netifera.platform.api.tasks.TaskOutputMessage;
import com.netifera.platform.model.ModelUpdate;
import com.netifera.platform.model.ProbeEntity;
import com.netifera.platform.tasks.messages.TaskChangedMessage;

/**
 * This service manages probes on the client side.
 */
public class ProbeManagerService implements IProbeManagerService {
	public static final int LOCAL_PROBE_ID = 0;

	private IModelService modelService;
	
	private final Set<ProbeClient> probeSet = 
		Collections.synchronizedSet(new HashSet<ProbeClient>());
	private ILogger logger;
	private IClientDispatcher clientDispatcher;
	private IChannelRegistry channelRegistry;
	private final EventListenerManager eventManager = new EventListenerManager();
	
	public ProbeClient getLocalProbe() {
		return getProbeById(LOCAL_PROBE_ID);
	}
	
	public List<IProbe> getProbeList() {
		return new ArrayList<IProbe>(probeSet);
	}
	
	public ProbeClient getProbeById(long probeId) {
		synchronized(probeSet) {
			for(ProbeClient probe : probeSet) {
				if(probe.getProbeId() == probeId)
					return probe;
			}
			return null;
		}
	}
	

	private IWorkspace getWorkspace() {
		if(modelService.getCurrentWorkspace() == null) {
			throw new IllegalStateException("Cannot initialize probe manager because no workspace is currently open");
		}
		return modelService.getCurrentWorkspace();
	}
	
	private void initialize() {
		logger.debug("Initializing ProbeManagerService");
		/* Make sure the current workspace has saved a local probe entity */
		final List<ProbeEntity> results = getWorkspace().findByPredicate(ProbeEntity.class,
				new IModelPredicate<ProbeEntity>() {
					public boolean match(ProbeEntity candidate) {
						return candidate.getProbeId() == LOCAL_PROBE_ID;
					}
				});
	
		/* if not, create it */
		if(results.isEmpty()) {
			createLocalProbeEntity();
		} 
		
		for(ProbeEntity p : getWorkspace().findAll(ProbeEntity.class)) {
			logger.debug("Adding probe with probe id: " + p.getProbeId() + " entity id: " + p.getId());
			ProbeClient probe = new ProbeClient(this, p, logger);
			probeSet.add(probe);
		}
		
		ProbeClient localProbe = getLocalProbe();
		if(localProbe == null) {
			throw new IllegalStateException("Local probe does not exist in model");
		}
		localProbe.connect();
		
	}
	
	/*
	 * This is where the local probe entity is created if it doesn't already exist in the currently opened workspace 
	 */
	private ProbeEntity createLocalProbeEntity() {
		logger.debug("Creating local probe entity");
		final ProbeEntity entity = new ProbeEntity(getWorkspace(), 0);
		entity.setProbeId(LOCAL_PROBE_ID);
		entity.setLocal(true);
		entity.setName("Local Probe");
		entity.setChannelConfig("local");
		entity.save();
		return entity;
	}
	
	public IProbe createProbe(IEntity hostEntity, String name, String config, long spaceId) {
		final ProbeEntity entity = new ProbeEntity(getWorkspace(), hostEntity);
		entity.setLocal(false);
		entity.setName(name);
		entity.setChannelConfig(config);
		entity.setProbeId(modelService.getCurrentWorkspace().generateProbeId());
		entity.save();
		
		((AbstractEntity)hostEntity).addTag("Controlled");
		((AbstractEntity)hostEntity).update();
//		((AbstractEntity)hostEntity).addToSpace(spaceId);
		
		final ProbeClient probe = new ProbeClient(this, entity, logger);
		probeSet.add(probe);
		eventManager.fireEvent(new IEvent() {});
		if(spaceId > 0)
			entity.addToSpace(spaceId);
		return probe;
	}
	
	public IProbe createProbe(String name, String config) {
		logger.debug("Creating probe named " + name);
		final ProbeEntity entity = new ProbeEntity(getWorkspace(), 0);
		entity.setLocal(false);
		entity.setName(name);
		entity.setChannelConfig(config);
		entity.setProbeId(modelService.getCurrentWorkspace().generateProbeId());
		entity.save();
		final ProbeClient probe = new ProbeClient(this, entity, logger);
		probeSet.add(probe);
		eventManager.fireEvent(new IEvent() {});
		return probe;
	}
	
	void fireChangeEvent() {
		eventManager.fireEvent(new IEvent() {});
	}
	
	public void addProbeChangeListener(IEventHandler handler) {
		eventManager.addListener(handler);
	}
	public void removeProbeChangeListener(IEventHandler handler) {
		eventManager.removeListener(handler);
	}

	IChannelConnecter createChannelConnecter(String config) {
		return channelRegistry.createConnecter(config);
	}
	
	private void registerHandlers(IMessageDispatcher dispatcher) {
		IMessageHandler handler = new IMessageHandler() {

			public void call(IMessenger messenger, IProbeMessage message)
					throws DispatchException {
				final ITaskClient taskClient = messenger.getProbe().getTaskClient();
				if(message instanceof TaskChangedMessage) {
					taskChangedHandler(taskClient, (TaskChangedMessage) message);
				} else if (message instanceof TaskOutputMessage) {
					taskOutputHandler(taskClient, (TaskOutputMessage) message);
				} else if (message instanceof ModelUpdate) {
					modelUpdateHandler(messenger, (ModelUpdate)message);
				} else {
					throw new DispatchMismatchException(message);
				}				
			}
		};
		
		dispatcher.registerMessageHandler(TaskChangedMessage.ID, handler);
		dispatcher.registerMessageHandler(TaskOutputMessage.ID, handler);
		dispatcher.registerMessageHandler(ModelUpdate.ID, handler);
	}
	
	
	private void taskChangedHandler(ITaskClient taskClient, TaskChangedMessage message) {
		for(ITaskStatus record : message.getTaskList()) 
			taskClient.taskChanged(record);
	}
	
	private void taskOutputHandler(ITaskClient taskClient, TaskOutputMessage message) {
		for(ITaskOutput output : message.getTaskOutput()) 
			taskClient.addMessage(output);
	}
	
	private void modelUpdateHandler(IMessenger messenger, ModelUpdate update) {
		ProbeClient probe = (ProbeClient) messenger.getProbe();
		probe.processModelUpdate(update);
	}
	
	/* OSGi service */
	
	private IEventHandler handler;
	protected void activate(ComponentContext ctx) {
		handler = new IEventHandler() {
			public void handleEvent(IEvent event) {
				initialize();				
			}
		};
		if(modelService.addWorkspaceOpenListener(handler)) 
			initialize();
		
	}
	
	protected void deactivate(ComponentContext ctx) {
		modelService.removeWorkspaceOpenListener(handler);
	}
	protected void setModelService(IModelService modelService) {
		this.modelService = modelService;		
	}
	
	protected void unsetModelService(IModelService modelService) {
		this.modelService = null;
	}
	
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Probe Manager");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		logger = null;
	}
	
	protected void setDispatcher(IMessageDispatcherService dispatcher) {
		clientDispatcher = dispatcher.getClientDispatcher();
		registerHandlers(clientDispatcher);
	}
	
	protected void unsetDispatcher(IMessageDispatcherService dispatcher) {
		clientDispatcher = null;
	}
	
	protected void setChannelRegistry(IChannelRegistry channelRegistry) {
		this.channelRegistry = channelRegistry;
	}
	
	protected void unsetChannelRegistry(IChannelRegistry channelRegistry) {
		this.channelRegistry = null;
	}

}
