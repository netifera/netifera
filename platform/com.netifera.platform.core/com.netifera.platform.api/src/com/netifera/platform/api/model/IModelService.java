package com.netifera.platform.api.model;

import java.util.List;

import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.model.layers.ILayerProvider;


public interface IModelService {
	
	boolean openWorkspace(String path);
	IWorkspace getCurrentWorkspace();	
	
	IEntityAdapterService getAdapterService();
	List<ILayerProvider> getLayerProviders();

	/**
	 * 
	 * @param listener
	 * @return true if a workspace is already open.
	 */
	boolean addWorkspaceOpenListener(IEventHandler listener);
	void removeWorkspaceOpenListener(IEventHandler listener);
}
