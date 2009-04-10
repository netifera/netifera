/**
 * 
 */
package com.netifera.platform.tasks.messages;

import java.util.List;

import com.netifera.platform.api.dispatcher.ProbeMessage;

/**
 * @author kevin
 *
 */
public class TasksSubscribeMessage extends ProbeMessage {
	public final static String ID = "TaskSubscribe";
	
	private static final long serialVersionUID = -6875821547502618275L;
	List<Long> taskIds;
	public TasksSubscribeMessage() {
		super(ID);
	}
	public TasksSubscribeMessage(List<Long> taskIds) {
		super(ID);
		this.taskIds = taskIds;
	}
	
	public List<Long> getTaskList() {
		return taskIds;
	}

}
