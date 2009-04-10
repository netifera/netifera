package com.netifera.platform.api.model.layers;

import com.netifera.platform.api.model.IEntity;

public interface IEdge {
	IEntity getSource();
	IEntity getTarget();
}
