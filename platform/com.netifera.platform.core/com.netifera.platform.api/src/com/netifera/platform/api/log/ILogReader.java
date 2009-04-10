package com.netifera.platform.api.log;

public interface ILogReader {
	void log(ILogEntry entry);
	void logRaw(String message);
}
