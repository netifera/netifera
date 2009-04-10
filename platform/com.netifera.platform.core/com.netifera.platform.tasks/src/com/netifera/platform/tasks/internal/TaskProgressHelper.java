package com.netifera.platform.tasks.internal;

import com.netifera.platform.tasks.TaskStatus;

public class TaskProgressHelper {

	private final TaskStatus taskStatus;
	private final TaskOutputHelper output;
	private int totalWork = -1;
	private int worked;
	private int lastWorkedUpdate;
	
	TaskProgressHelper(Task task, TaskOutputHelper output) {
		this.taskStatus = task.getStatus();
		this.output = output;
	}
	
	void setTotalWork(int totalWork) {
		this.totalWork = totalWork;
		worked = 0;
		taskStatus.setWorkDone(0);
		output.changed();
	}
	
	void done() {
		taskStatus.updateElapsedTime();
		worked(totalWork - worked);
	}
	
	void worked(int work) {
//		if (totalWork<0)
//			throw new RuntimeException("Trying to update progress, but totalWork was not set");
		worked+=work;
		taskStatus.setWorkDone(worked*100/totalWork);
		/* notify change if more than 2% increment since last update */
		if((worked-lastWorkedUpdate)*100/totalWork >= 2) {
			lastWorkedUpdate = worked;
			output.changed();
		}
	}
}
