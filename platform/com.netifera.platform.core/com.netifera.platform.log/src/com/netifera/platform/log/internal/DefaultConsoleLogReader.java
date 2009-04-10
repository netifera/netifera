package com.netifera.platform.log.internal;

import com.netifera.platform.api.log.ILogEntry;
import com.netifera.platform.api.log.ILogReader;
import com.netifera.platform.api.log.ILogEntry.LogLevel;

public class DefaultConsoleLogReader implements ILogReader {

	
	public void log(ILogEntry entry) {
		final OutputState out = new OutputState(entry);
		
		addBanner(out);
		out.println(entry.getMessage());
		addException(out);
		printToConsole(out);
		
	}

	public void logRaw(String message) {
		System.out.println(message);
	}
	
	private void addBanner(OutputState out) {
		final ILogEntry entry = out.getEntry();
		
		switch(entry.getLevel()) {
		case DEBUG:
			out.print("DEBUG");
			break;
		case INFO:
			out.print("INFO");
			break;
		case WARNING:
			out.print("WARN");
			break;
		case ERROR:
			out.print("ERROR");
			break;
		}
		
		out.print(" (" + entry.getComponent() + ") : ");
	}
	
	private void addException(OutputState out) {
		final ILogEntry entry = out.getEntry();
		if(entry.getException() != null) {
			out.printException(entry.getException());
		}
	}
	
	private void printToConsole(OutputState out) {
		final ILogEntry entry = out.getEntry();
		if(entry.getLevel() == LogLevel.ERROR) {
			System.err.print(out);
		} else {
			System.out.print(out);
		}
	}
}
