package com.netifera.platform.api.model;

import com.netifera.platform.api.tasks.ITaskOutput;
import com.netifera.platform.api.tasks.ITaskRecord;

public class SpaceTaskChangeEvent implements ISpaceTaskChangeEvent {

	private final boolean isUpdate;
	private final boolean isCreation;
	private final ITaskRecord task;
	private final ITaskOutput output;
	
	public static SpaceTaskChangeEvent createUpdateEvent(ITaskRecord task) {
		return new SpaceTaskChangeEvent(task, null, true, false);
	}
	
	public static SpaceTaskChangeEvent createCreationEvent(ITaskRecord task) {
		return new SpaceTaskChangeEvent(task, null, false, true);
	}
	
	private SpaceTaskChangeEvent(ITaskRecord task, ITaskOutput output, boolean update, boolean creation) {
		this.task = task;
		this.output = output;
		this.isUpdate = update;
		this.isCreation = creation;
	}
	
	public ITaskRecord getTask() {
		return task;
	}
	
	public ITaskOutput getOutput() {
		return output;
	}

	public boolean isCreationEvent() {
		return isCreation;
	}

	public boolean isUpdateEvent() {
		return isUpdate;
	}
	
	public boolean isOutputEvent() {
		return output != null;
	}

}
