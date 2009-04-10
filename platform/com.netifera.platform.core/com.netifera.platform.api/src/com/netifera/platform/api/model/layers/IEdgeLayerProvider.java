package com.netifera.platform.api.model.layers;

import java.util.List;

import com.netifera.platform.api.model.IEntity;

public interface IEdgeLayerProvider extends ILayerProvider {
	List<IEdge> getEdges(IEntity entity);
}
