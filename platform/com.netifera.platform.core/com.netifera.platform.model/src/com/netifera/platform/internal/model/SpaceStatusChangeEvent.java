package com.netifera.platform.internal.model;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.ISpaceStatusChangeEvent;

public class SpaceStatusChangeEvent implements ISpaceStatusChangeEvent {
	final private ISpace space;
	final private boolean isNew;
	final private boolean isChanged;
	
	
	public static SpaceStatusChangeEvent createNewEvent(ISpace space) {
		return new SpaceStatusChangeEvent(space, true, false);
	}
	
	public static SpaceStatusChangeEvent createChangedEvent(ISpace space) {
		return new SpaceStatusChangeEvent(space, false, true);
	}
	
	private SpaceStatusChangeEvent(ISpace space, boolean isNew, boolean isChanged) {
		this.space = space;
		this.isNew = isNew;
		this.isChanged = isChanged;
	}
	
	public ISpace getSpace() {
		return space;
	}

	public boolean isNew() {
		return isNew;
	}
	
	public boolean isChanged() {
		return isChanged;
	}
}
