package com.netifera.platform.api.model.layers;

import java.util.Set;

import com.netifera.platform.api.model.IEntity;

public interface IGroupLayerProvider extends ILayerProvider {
	Set<String> getGroups(IEntity entity);
}
