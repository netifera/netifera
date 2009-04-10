package com.netifera.platform.api.tasks;

import com.netifera.platform.api.events.IEvent;

public interface ITaskOutputEvent extends IEvent {

	ITaskOutput getMessage();

}