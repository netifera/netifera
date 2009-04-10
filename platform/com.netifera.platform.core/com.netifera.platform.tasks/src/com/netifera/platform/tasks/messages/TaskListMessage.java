package com.netifera.platform.tasks.messages;

import com.netifera.platform.api.dispatcher.ProbeMessage;
import com.netifera.platform.api.tasks.ITaskStatus;

public class TaskListMessage extends ProbeMessage {
	
	private static final long serialVersionUID = -1248655320483633560L;

	public final static String ID = "TaskList";

	private final ITaskStatus taskList[];

	public TaskListMessage() {
		super(ID);
		taskList = new ITaskStatus[0];
	}
	
	public TaskListMessage createResponse(ITaskStatus taskList[]) {
		return new TaskListMessage(taskList, getSequenceNumber());
	}
	
	private TaskListMessage(ITaskStatus taskList[], int sequenceNumber) {
		super(ID);
		this.taskList = taskList;
		setSequenceNumber(sequenceNumber);
		markAsResponse();
	}
	
	public ITaskStatus[] getTaskList() {
		return taskList;
	}
}
