package com.netifera.platform.internal.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.ComponentContext;

import com.netifera.platform.api.events.EventListenerManager;
import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.IEntityAdapterService;
import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.api.model.layers.ILayerProvider;

public class ModelService implements IModelService {

	private final static String DATABASE_NAME = "model.db";
	
	//private Db4oService dbService;
	
	private DatabaseConfigurationFactory factory;
	private IWorkspace currentWorkspace;
	private final List<ILayerProvider> layerProviders = new ArrayList<ILayerProvider>();
	private IEntityAdapterService entityAdapterService;
	private ILogger logger;
	private transient EventListenerManager workspaceOpenListeners = new EventListenerManager();

		
	
	public boolean openWorkspace(String path) {
		if(currentWorkspace != null) {
			throw new IllegalStateException("Workspace has already been opened.");
		}
		
		createIfNeeded(path);
		
		final IWorkspace workspace = new Workspace(this, getDatabasePath(path));
		
		if(!workspace.open()) {
			return false;
		}
		synchronized(workspaceOpenListeners) {
			currentWorkspace = workspace;
			workspaceOpenListeners.fireEvent(new IEvent() {});
		}
		return true;
	}
	
	public boolean addWorkspaceOpenListener(IEventHandler listener) {
		synchronized(workspaceOpenListeners) {
			workspaceOpenListeners.addListener(listener);
			return currentWorkspace != null;
		}
	}
	
	public void removeWorkspaceOpenListener(IEventHandler listener) {
		workspaceOpenListeners.removeListener(listener);
	}
	
	public IWorkspace getCurrentWorkspace() {
		if(currentWorkspace == null && !openWorkspaceFromProperty()) {
			throw new IllegalStateException("Workspace is not opened.");
		}
		return currentWorkspace;
	}

	public List<ILayerProvider> getLayerProviders() {
		return layerProviders;
	}
	
	protected void activate(final ComponentContext ctx) {
		logger.debug("Activating model");
		factory = new DatabaseConfigurationFactory();
	}
	
	private boolean openWorkspaceFromProperty() {
		String path = System.getProperty("netifera.model.path");
		if(path == null)
			return false;
		
		if(!openWorkspace(path)) { 
			logger.warning("Failed to open workspace at path " + path);
			return false;
		}
		return true;
	}

	protected void deactivate(ComponentContext ctx) {
		if(currentWorkspace != null) {
			currentWorkspace.close();
			currentWorkspace = null;
		}
	}
	
	private boolean createIfNeeded(String workspacePath) {
		return new File(workspacePath).mkdirs();
	}
	
	private String getDatabasePath(String workspacePath) {
		if(workspacePath.endsWith(File.separator)) {
			workspacePath = workspacePath.substring(0, workspacePath.length() - 1);
		}
		return workspacePath + File.separator + DATABASE_NAME;
	}
	
	DatabaseConfigurationFactory getDatabaseFactory() {
		return factory;
	}
	
	ILogger getLogger() {
		return logger;
	}
	
	/*
	protected void registerDb4o(Db4oService dbService) {
		this.dbService = dbService;
		
	}
	
	protected void unregisterDb4o(Db4oService dbService) {
		this.dbService = null;
	}
	*/
	protected void registerLayerProvider(ILayerProvider provider) {
		layerProviders.add(provider);
	}
	protected void unregisterLayerProvider(ILayerProvider provider) {
		layerProviders.remove(provider);
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Model");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}
	
	protected void setEntityAdapterService(IEntityAdapterService service) {
		entityAdapterService = service;
	}

	protected void unsetEntityAdapterService(IEntityAdapterService service) {
		entityAdapterService = null;
	}

	public IEntityAdapterService getAdapterService() {
		return entityAdapterService;
	}
}
