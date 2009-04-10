package com.netifera.platform.api.tasks;

public interface ITaskStatus {
	
	void update(ITaskStatus newStatus);

	String getTitle();
	String getStatus();

    String getStateDescription();

    String getInstanceClass();

	long getTaskId();

	int getRunState();

    boolean isRunning();
    
    boolean isWaiting();
    
    boolean isFinished();
    
    boolean isFailed();

	long getStartTime();

	long getElapsedTime();

	int getWorkDone();
}