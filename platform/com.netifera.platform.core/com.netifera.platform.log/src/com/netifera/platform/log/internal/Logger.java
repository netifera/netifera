package com.netifera.platform.log.internal;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogReader;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.log.ILogEntry.LogLevel;

public class Logger implements ILogger {
	
	private final String name;
	private final ILogReader reader;
	private final LogManager manager;
	private boolean debugEnabled;
	
	Logger(String name, LogManager manager) {
		this.name = name;
		this.reader = manager;
		this.manager = manager;
		debugEnabled = false;
	}

	public ILogManager getManager() {
		return manager;
	}
	
	public void debug(String message) {
		debug(message, null);
	}

	public void debug(String message, Throwable exception) {
		if(debugEnabled) {
			log(LogLevel.DEBUG, message, exception);
		}
	}

	public void enableDebug() {
		debugEnabled = true;
	}
	
	public void disableDebug() {
		debugEnabled = false;
	}
	
	public void error(String message) {
		error(message, null);		
	}

	public void error(String message, Throwable exception) {
		log(LogLevel.ERROR, message, exception);
	}

	public void info(String message) {
		info(message, null);		
	}

	public void info(String message, Throwable exception) {
		log(LogLevel.INFO, message, exception);		
	}

	public void warning(String message) {
		warning(message, null);		
	}

	public void warning(String message, Throwable exception) {
		log(LogLevel.WARNING, message, exception);		
	}
	
	private void log(LogLevel level, String message, Throwable exception) {
		if(reader != null) {
			reader.log(new LogEntry(level, name, message, exception));
		}
	}

}
