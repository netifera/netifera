package com.netifera.platform.internal.model;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.model.IUpdateRecord;

public class UpdateRecord implements IUpdateRecord {

	private final IEntityReference entityReference;
	private final IWorkspace workspace;
	private final boolean addToSpace;
	private final long spaceId;
	private final long updateIndex;
	
	public static UpdateRecord create(IWorkspace workspace, IEntity entity, long idx) {
		return new UpdateRecord(workspace, entity, false, 0, idx);
	}
	
	public static UpdateRecord createAddedToSpace(IWorkspace workspace, IEntity entity, long spaceId, long idx) {
		return new UpdateRecord(workspace, entity, true, spaceId, idx);
	}
	
	private UpdateRecord(IWorkspace workspace, IEntity entity, boolean addToSpace, long spaceId, long idx) {
		this.workspace = workspace;
		this.entityReference = entity.createReference();
		this.addToSpace = addToSpace;
		this.spaceId = spaceId;
		this.updateIndex = idx;
	}
	
	public boolean isAddedToSpace() {
		return addToSpace;
	}
	
	public long getSpaceId() {
		return spaceId;
	}
	
	public IEntity getEntity() {
		final IEntity entity = entityReference.getEntity(workspace);
		entityReference.freeCachedEntity();
		return entity;
	}
	
	public long getUpdateIndex() {
		return updateIndex;
	}
	
	public String toString() {
		if(addToSpace) {
			return "Update (add to space " + spaceId + ") " + getEntity();
		} else {
			return "Update " + getEntity();
		}
	}

	public IUpdateRecord getTransferRecord() {
		return new UpdateTransferRecord(getEntity(), spaceId, updateIndex, addToSpace);
	}
	
}
