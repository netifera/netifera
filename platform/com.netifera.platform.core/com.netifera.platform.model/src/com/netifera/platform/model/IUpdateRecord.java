package com.netifera.platform.model;

import com.netifera.platform.api.model.IEntity;

public interface IUpdateRecord {
	
	boolean isAddedToSpace();
	long getSpaceId();
	IEntity getEntity();
	long getUpdateIndex();

}
