package com.netifera.platform.tasks;

import com.netifera.platform.api.tasks.TaskOutput;

public class TaskConsoleOutput extends TaskOutput {
	
	private static final long serialVersionUID = 1766838422136590865L;
	private final String message;
	
	public TaskConsoleOutput(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}

}
