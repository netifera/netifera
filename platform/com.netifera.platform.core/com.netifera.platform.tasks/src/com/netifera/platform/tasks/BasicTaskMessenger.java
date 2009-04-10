package com.netifera.platform.tasks;

import com.netifera.platform.api.tasks.ITaskMessenger;
import com.netifera.platform.api.tasks.ITaskOutput;

public class BasicTaskMessenger implements ITaskMessenger {
	final private ITaskMessenger messenger;
	
	public BasicTaskMessenger(ITaskMessenger messenger) {
		this.messenger = messenger;
	}
	
	public void addMessage(ITaskOutput message) {
		messenger.addMessage(message);
	}
	
	/** Task Logging methods could be implemented in ITaskLogging interface and delegate */

	public void debug(String message) {
		log(TaskLogOutput.DEBUG, message);
	}

	public void info(String message) {
		log(TaskLogOutput.INFO, message);
	}

	public void warning(String message) {
		log(TaskLogOutput.WARNING, message);
	}

	public void error(String message) {
		log(TaskLogOutput.ERROR, message);
	}

	public void log(int logLevel, String message) {
		addMessage(new TaskLogOutput(logLevel, message));
	}
}