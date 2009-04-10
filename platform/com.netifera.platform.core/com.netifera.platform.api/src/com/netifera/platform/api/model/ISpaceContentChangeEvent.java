package com.netifera.platform.api.model;

import com.netifera.platform.api.events.IEvent;

public interface ISpaceContentChangeEvent extends IEvent {

	IEntity getEntity();

	boolean isCreationEvent();
	boolean isUpdateEvent();
	boolean isRemovalEvent();
}