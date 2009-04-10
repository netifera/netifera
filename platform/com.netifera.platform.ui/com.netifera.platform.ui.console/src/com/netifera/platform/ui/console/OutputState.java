package com.netifera.platform.ui.console;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.netifera.platform.api.log.ILogEntry;

public class OutputState {
	private final StringWriter stringWriter;
	private final PrintWriter printWriter;
	private final ILogEntry entry;
	
	OutputState(ILogEntry entry) {
		this.entry = entry;
		stringWriter = new StringWriter();
		printWriter = new PrintWriter(stringWriter);
	}
	
	ILogEntry getEntry() {
		return entry;
	}
	
	void print(String s) {
		printWriter.print(s);
	}
	
	void println(String s) {
		printWriter.println(s);
	}
	
	void printException(Throwable exception) {
		if(exception.getMessage() != null) {
			printWriter.println(exception.getMessage());
		}
		exception.printStackTrace(printWriter);
	}
	
	public String toString() {
		printWriter.flush();
		return stringWriter.toString();
	}
	

}
