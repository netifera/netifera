package com.netifera.platform.api.tasks;

import java.util.List;

import com.netifera.platform.api.events.IEventHandler;

public interface ITaskRecord {
	
	void addTaskOutputListener(IEventHandler handler);
	void removeTaskOutputListener(IEventHandler handler);

	List<ITaskOutput> getTaskOutput();

	void updateTaskStatus(ITaskStatus status);
	void addTaskOutput(ITaskOutput output);
	String getTitle();
	String getStatus();
	int getWorkDone();

	String getStateDescription();
	long getTaskId();
	long getProbeId();
	long getStartTime();
	long getElapsedTime();
	int getRunState();
	boolean isWaiting();
	boolean isRunning();
	boolean isFinished();
	boolean isFailed();

}
