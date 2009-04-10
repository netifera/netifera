package com.netifera.platform.api.tasks;


public interface ITask {
	long getTaskId();
	void start();
	
	void setTotalWork(int totalWork);
	void worked(int work);
	void done();
	
	void setTitle(String title);
	void setStatus(String status);

	void debug(String message);
	void info(String message);
	void warning(String message);
	void error(String message);
	
	void print(String message);
	void exception(String message, Throwable throwable);
	
}
