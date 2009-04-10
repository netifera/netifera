package com.netifera.platform.net.internal.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.layers.IGroupLayerProvider;
import com.netifera.platform.net.model.ServiceEntity;

public class ServicesByTypeLayerProvider implements IGroupLayerProvider {

	public Set<String> getGroups(IEntity entity) {
		if (entity instanceof ServiceEntity) {
			String type = ((ServiceEntity)entity).getServiceType();
			if (type != null) {
				Set<String> answer = new HashSet<String>();
				answer.add(type);
				return answer;
			}
		}
		
		return Collections.emptySet();
	}
	
	public String getLayerName() {
		return "Services By Type";
	}

	public boolean isDefaultEnabled() {
		return false;
	}
}
