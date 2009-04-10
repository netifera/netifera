package com.netifera.platform.api.log;

public interface ILogger {
	void error(String message);
	
	void error(String message, Throwable exception);
	
	void warning(String message);
	
	void warning(String message, Throwable exception);
	
	void info(String message);
	
	void info(String message, Throwable exception);
	
	void debug(String message);
	
	void debug(String message, Throwable exception);
	
	void enableDebug();
	
	void disableDebug();
	
	ILogManager getManager();

}
