package com.netifera.platform.api.model;

import com.netifera.platform.api.events.IEvent;

public class SpaceNameChangeEvent implements IEvent {
	private final String name;

	public SpaceNameChangeEvent(String newName) {
		this.name = newName;
	}

	public String getName() {
		return name;
	}
}
