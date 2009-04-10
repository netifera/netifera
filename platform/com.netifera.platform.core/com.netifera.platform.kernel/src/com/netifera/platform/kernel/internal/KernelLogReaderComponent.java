package com.netifera.platform.kernel.internal;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

public class KernelLogReaderComponent implements LogListener {
	private final static int level = LogService.LOG_DEBUG;
	private final static String[] filters = { "BundleEvent", "ServiceEvent", "FrameworkEvent" };
	public void logged(LogEntry entry) {
		if(filtered(entry)) return;
		
		StringWriter output = new StringWriter();
		PrintWriter pw = new PrintWriter(output);
		
		if(entry.getLevel() > level) {
			return;
		}
		switch(entry.getLevel()) {
		case LogService.LOG_DEBUG:
			pw.print("DEBUG: ");
			break;
		case LogService.LOG_INFO:
			pw.print("INFO: ");
			break;
		case LogService.LOG_WARNING:
			pw.print("WARNING: ");
			break;
		case LogService.LOG_ERROR:
			pw.print("ERROR: ");
			break;
		}
		
		pw.print(entry.getMessage());
		Throwable exception = entry.getException();
		if(exception != null) {
			if(exception.getMessage() != null) {
				pw.println(exception.getMessage());
			}
			exception.printStackTrace(pw);
		}
		pw.flush();
		
		switch(entry.getLevel()) {
		case LogService.LOG_ERROR:
			System.err.println(output.toString());
			break;
		default:
			System.out.println(output.toString());
		}
	}
	
	private boolean filtered(LogEntry entry) {
		for(String prefix : filters) {
			if(entry.getMessage().startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}
	
	protected void registerLogReader(LogReaderService reader) {
		reader.addLogListener(this);
	}
	protected void unregisterLogReader(LogReaderService reader) {
		reader.removeLogListener(this);
	}

}
