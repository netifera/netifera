package com.netifera.platform.tasks.messages;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class TaskStartMessage extends ProbeMessage {
	
	private static final long serialVersionUID = 160827117303179127L;

	public final static String ID = "TaskStart";

	private final long taskId;
	
	public TaskStartMessage(long taskId) {
		super(ID);
		this.taskId = taskId;
	}
	
	public long getTaskId() {
		return taskId;
	}

}
