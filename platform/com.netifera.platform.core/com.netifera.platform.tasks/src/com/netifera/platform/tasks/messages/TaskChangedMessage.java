
package com.netifera.platform.tasks.messages;


import com.netifera.platform.api.dispatcher.ProbeMessage;
import com.netifera.platform.api.tasks.ITaskStatus;

public class TaskChangedMessage extends ProbeMessage {
	public final static String ID = "TaskChange";
	
	private static final long serialVersionUID = -2875841506502518268L;
	private final ITaskStatus taskList[];
	
	public TaskChangedMessage(ITaskStatus taskList[]) {
		super(ID);
		this.taskList = taskList; 
	}
	
	public ITaskStatus[] getTaskList() {
		return taskList;
	}

}
