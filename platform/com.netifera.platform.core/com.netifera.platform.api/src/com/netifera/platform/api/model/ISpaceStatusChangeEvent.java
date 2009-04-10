package com.netifera.platform.api.model;

import com.netifera.platform.api.events.IEvent;

public interface ISpaceStatusChangeEvent extends IEvent {
	ISpace getSpace();
	boolean isNew();
	boolean isChanged();
}
