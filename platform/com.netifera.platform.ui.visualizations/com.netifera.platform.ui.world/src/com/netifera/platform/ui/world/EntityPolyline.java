package com.netifera.platform.ui.world;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Polyline;

import com.netifera.platform.api.model.IEntity;

public class EntityPolyline extends Polyline {

	final private IEntity entity;
	
	public EntityPolyline(IEntity entity, Iterable<Position> positions) {
		super(positions);
		this.entity = entity;
	}
	
	public IEntity getEntity() {
		return entity;
	}
}
