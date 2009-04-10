package com.netifera.platform.api.model.layers;

import com.netifera.platform.api.model.IEntity;

public interface ITreeLayerProvider extends ILayerProvider {
	boolean isRealmRoot(IEntity entity);
	IEntity[] getParents(IEntity entity);
}
