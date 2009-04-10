package com.netifera.platform.internal.model;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.layers.ITreeLayerProvider;
import com.netifera.platform.model.ProbeEntity;

public class TreeLayerProvider implements ITreeLayerProvider {

	public IEntity[] getParents(IEntity entity) {
		return new IEntity[0];
	}

	public boolean isRealmRoot(IEntity entity) {
		return (entity instanceof ProbeEntity) && !((ProbeEntity)entity).isLocal();
	}

	public String getLayerName() {
		return "Probes";
	}

	public boolean isDefaultEnabled() {
		return true;
	}
}
