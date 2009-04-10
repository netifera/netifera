package com.netifera.platform.api.tasks;



public interface ITaskRunnable {
	void run(ITask task) throws TaskException;
	/* The name of the class invoked by this runnable */
	String getClassName();

}
