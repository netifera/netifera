package com.netifera.platform.net.geoip;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.layers.ILayerProvider;

public interface IGeographicalLayerProvider extends ILayerProvider {
	ILocation getLocation(IEntity entity);
}
