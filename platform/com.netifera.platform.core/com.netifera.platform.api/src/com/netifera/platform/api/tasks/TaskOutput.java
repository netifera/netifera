/**
 * 
 */
package com.netifera.platform.api.tasks;

import java.io.Serializable;




/**
 * @author kevin
 *
 */
public class TaskOutput implements Serializable, ITaskOutput, Comparable<ITaskOutput> {
	
	private static final long serialVersionUID = 8031982252649918270L;
	
	protected long taskId;
	protected long time;
	private transient boolean initialized = false;
	
	
	public void initialize(final long taskId) {
		this.taskId = taskId;
		time = System.currentTimeMillis();
		initialized = true;
	}
	
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public long getTaskId() {
		return taskId;
	}
	
	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	
	public long getTime() {
		return time;
	}
	
	public ITaskOutput merge(final ITaskOutput other) {
		return null;
	}

	public int compareTo(final ITaskOutput o) {
		
		if (time < o.getTime()) {
			return -1;
		}
		
		if(time > o.getTime()) {
			return 1;
		}
		return 0;
	}	
}
