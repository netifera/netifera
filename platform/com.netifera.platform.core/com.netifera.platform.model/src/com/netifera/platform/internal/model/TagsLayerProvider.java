package com.netifera.platform.internal.model;

import java.util.Set;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.layers.IGroupLayerProvider;

public class TagsLayerProvider implements IGroupLayerProvider {
	
	public Set<String> getGroups(IEntity entity) {
		return entity.getTags();
	}

	public String getLayerName() {
		return "Tags";
	}

	public boolean isDefaultEnabled() {
		return true;
	}
}
