package com.netifera.platform.ui.internal.spaces.visualizations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.ui.spaces.visualizations.ISpaceVisualization;
import com.netifera.platform.ui.spaces.visualizations.ISpaceVisualizationFactory;
import com.netifera.platform.ui.spaces.visualizations.ISpaceVisualizationProvider;

public class SpaceVisualizationFactory implements ISpaceVisualizationFactory {
	private final Map<String,ISpaceVisualizationProvider> providers = new HashMap<String,ISpaceVisualizationProvider>();
	
	public synchronized ISpaceVisualization create(String name, ISpace space) {
		return providers.get(name).create(space);
	}
	
	public synchronized Set<String> getVisualizationNames() {
		return providers.keySet();
	}
	
	protected synchronized void registerProvider(ISpaceVisualizationProvider provider) {
		providers.put(provider.getName(), provider);
	}
	
	protected synchronized void unregisterProvider(ISpaceVisualizationProvider provider) {
		providers.remove(provider.getName());
	}
}
