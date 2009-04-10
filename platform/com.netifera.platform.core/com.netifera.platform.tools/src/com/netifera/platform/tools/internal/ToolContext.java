package com.netifera.platform.tools.internal;

import com.netifera.platform.api.tasks.ITask;
import com.netifera.platform.api.tasks.ITaskMessenger;
import com.netifera.platform.api.tasks.ITaskOutput;
import com.netifera.platform.api.tasks.ITaskRunnable;
import com.netifera.platform.api.tasks.TaskException;
import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolConfiguration;
import com.netifera.platform.api.tools.IToolContext;

public class ToolContext implements IToolContext, ITaskRunnable, ITaskMessenger {
	private ITool toolInstance;
	private IToolConfiguration configuration;
	private ITask task;
	private final long spaceId;
	private boolean debugEnabled;
	
	ToolContext(ITool tool, IToolConfiguration configuration, long spaceId) {
		toolInstance = tool;
		this.configuration = configuration;
		this.spaceId = spaceId;
		this.debugEnabled = false;
	}
	
	public void setTitle(String title) {
		task.setTitle(title);
	}

	public void setStatus(String status) {
		task.setStatus(status);
	}

	public long getSpaceId() {
		return spaceId;
	}
	
	public void run(ITask task) throws TaskException {
		this.task = task;
		toolInstance.toolRun(this);
	}

	public IToolConfiguration getConfiguration() {
		return configuration;
	}

	public void addMessage(ITaskOutput command) {
		ITaskMessenger messenger = (ITaskMessenger) task;
		messenger.addMessage(command);		
	}

	public void enableDebugOutput() {
		debugEnabled = true;
	}
	
	public void debug(String message) {
		if(debugEnabled) {
			task.debug(message);
		}
	}

	public void setTotalWork(int totalWork) {
		task.setTotalWork(totalWork);
	}

	public void worked(int work) {
		task.worked(work);		
	}

	public void done() {
		task.done();		
	}

	public void error(String message) {
		task.error(message);
	}

	public void info(String message) {
		task.info(message);
	}

	public void print(String message) {
		task.print(message);
	}
	
	public void exception(String message, Throwable throwable) {
		task.exception(message, throwable);
	}
	
	public void warning(String message) {
		task.warning(message);
	}

	public String getClassName() {
		return toolInstance.getClass().getName();
	}
}
