package com.netifera.platform.internal.model;

import java.io.Serializable;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.model.IUpdateRecord;

public class UpdateTransferRecord implements IUpdateRecord, Serializable {

	private static final long serialVersionUID = 1848715332481892585L;
	private final IEntity entity;
	private final long spaceId;
	private final long updateIndex;
	private final boolean isAddedToSpace;
	
	public UpdateTransferRecord(IEntity entity, long spaceId, long updateIndex, boolean isAddedToSpace) {
		this.entity = entity;
		this.spaceId = spaceId;
		this.updateIndex = updateIndex;
		this.isAddedToSpace = isAddedToSpace;		
	}
	public IEntity getEntity() {
		return entity;
	}

	public long getSpaceId() {
		return spaceId;
	}

	public long getUpdateIndex() {
		return updateIndex;
	}

	public boolean isAddedToSpace() {
		return isAddedToSpace;
	}

}
