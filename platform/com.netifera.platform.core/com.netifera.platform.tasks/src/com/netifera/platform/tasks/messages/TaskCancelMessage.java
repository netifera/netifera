/**
 * 
 */
package com.netifera.platform.tasks.messages;

import com.netifera.platform.api.dispatcher.ProbeMessage;

/**
 * @author kevin
 *
 */
public class TaskCancelMessage extends ProbeMessage {
	public final static String ID = "TaskCancel";
	private static final long serialVersionUID = -4117414094435120599L;
	private long taskIdList[];
	
	public TaskCancelMessage() {
		super(ID);
	}
	public TaskCancelMessage(long taskIdList[]) {
		super(ID);
		this.taskIdList = taskIdList; 
	}
	public TaskCancelMessage createResponse(long taskIdList[]) {
		/* XXX why return taskIdList? */
		return new TaskCancelMessage(taskIdList, getSequenceNumber());
	}
	
	private TaskCancelMessage(long taskIdList[], int sequenceNumber) {
		super(ID);
		this.taskIdList = taskIdList;
		setSequenceNumber(sequenceNumber);
		markAsResponse();
	}
	
	public long[] getTaskList() {
		return taskIdList;
	}
	

}
