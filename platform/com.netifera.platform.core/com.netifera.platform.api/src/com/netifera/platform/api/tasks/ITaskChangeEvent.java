package com.netifera.platform.api.tasks;

import com.netifera.platform.api.events.IEvent;

public interface ITaskChangeEvent extends IEvent {

	ITaskStatus getTaskRecord();
	
	boolean isCreationEvent();

}