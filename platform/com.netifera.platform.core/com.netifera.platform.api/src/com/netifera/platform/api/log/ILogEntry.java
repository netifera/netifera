package com.netifera.platform.api.log;

public interface ILogEntry {
	enum LogLevel { ERROR, WARNING, INFO, DEBUG };
	LogLevel getLevel();
	String getComponent();
	String getMessage();
	Throwable getException();
}
