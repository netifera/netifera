package com.netifera.platform.log.internal;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.ComponentContext;

import com.netifera.platform.api.log.ILogEntry;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogReader;
import com.netifera.platform.api.log.ILogger;

public class LogManager implements ILogManager, ILogReader {

	private final Map<String, ILogger> loggers = new HashMap<String, ILogger>();
	private ILogReader logReader = new DefaultConsoleLogReader();
	protected void activate(ComponentContext context) {
		if(System.getProperty("netifera.consolelog") != null) {
			logReader = new DefaultConsoleLogReader();
		} else {
			logReader = new RingBufferReader();
		}
	}
	
	protected void deactivate(ComponentContext context) {
		
	}
	
	public synchronized ILogger getLogger(String name) {
		if(!loggers.containsKey(name)) {
			loggers.put(name, new Logger(name, this));
		}
		return loggers.get(name);
	}

	public void logRaw(String message) {
		if(logReader != null) {
			synchronized(logReader) {
				logReader.logRaw(message);
			}
		}
	}
	
	public void log(ILogEntry entry) {
		if(logReader != null) {
			synchronized(logReader) {
				logReader.log(entry);
			}
		}		
	}
	
	public void setLogReader(ILogReader reader) {
		if(reader == null) {
			this.logReader = reader;
			return;
		}
		synchronized(logReader) {
			if(logReader instanceof RingBufferReader) {
				RingBufferReader ringBufferReader = (RingBufferReader) logReader;
				for(ILogEntry entry : ringBufferReader) {
					reader.log(entry);
				}
			}
			this.logReader = reader;
		}
	}
}
