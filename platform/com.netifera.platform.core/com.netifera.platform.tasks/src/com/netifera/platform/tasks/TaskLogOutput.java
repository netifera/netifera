package com.netifera.platform.tasks;

import com.netifera.platform.api.tasks.TaskOutput;

public class TaskLogOutput extends TaskOutput {
	private static final long serialVersionUID = -3026196486598416765L;
    public final static int DEBUG =   0;
    public final static int INFO =    1;
    public final static int WARNING = 2;
    public final static int ERROR =   3;

	private final int logLevel;
	private final String message;
	
	public TaskLogOutput(int logLevel, String message) {
		this.message = message;
		this.logLevel = logLevel;
	}
	
	public String getMessage() {
		return message;
	}
	
	@Override
    public String toString() {
		return message;
	}
	
	public boolean isDebug() {
	    return logLevel == DEBUG;
	}

    public boolean isInfo() {
        return logLevel == INFO;
    }

    public boolean isWarning() {
        return logLevel == WARNING;
    }

    public boolean isError() {
        return logLevel == ERROR;
    }
    
    public int getLogLevel() {
    	return logLevel;
    }
}
