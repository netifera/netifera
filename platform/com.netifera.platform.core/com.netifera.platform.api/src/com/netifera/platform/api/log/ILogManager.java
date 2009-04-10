package com.netifera.platform.api.log;

public interface ILogManager {
	ILogger getLogger(String name);
	void setLogReader(ILogReader reader);
	void logRaw(String message);
}
