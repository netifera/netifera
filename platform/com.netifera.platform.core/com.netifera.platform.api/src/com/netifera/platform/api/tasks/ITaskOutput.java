package com.netifera.platform.api.tasks;

public interface ITaskOutput extends Comparable<ITaskOutput> {

	void initialize(long taskId);

	boolean isInitialized();

	long getTaskId();
	void setTaskId(long taskId);

	long getTime();

}