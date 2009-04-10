package com.netifera.platform.internal.model;

import java.io.Serializable;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;

public class EntityReference implements IEntityReference, Serializable {

	private static final long serialVersionUID = 454593980781826497L;
	
	private final long entityId;
	transient private IEntity cachedEntity;
	
	public static EntityReference create(long id) {
		if(id <= 0) {
			throw new IllegalArgumentException();
		}
		return new EntityReference(id);
	}
	
	public EntityReference(long id) {
		this.entityId = id;
		cachedEntity = null;
	}
	public IEntityReference createClone() {
		return new EntityReference(entityId);
	}

	public IEntity getEntity(IWorkspace workspace) {
		if(cachedEntity == null) {
			cachedEntity = workspace.findById(entityId);
		}
		return cachedEntity;
	}

	public void freeCachedEntity() {
		cachedEntity = null;
	}
	
	public long getId() {
		return entityId;
	}
	
	public int hashCode() {
		return (int) entityId;
	}
	
	public boolean equals(Object o) {
		return (o instanceof EntityReference) && entityId == ((EntityReference)o).getId();
	}
	
}
