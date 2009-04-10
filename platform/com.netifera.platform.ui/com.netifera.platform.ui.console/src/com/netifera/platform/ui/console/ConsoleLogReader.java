package com.netifera.platform.ui.console;

import org.eclipse.swt.widgets.Display;

import com.netifera.platform.api.log.ILogEntry;
import com.netifera.platform.api.log.ILogReader;

public class ConsoleLogReader implements ILogReader{
	private final ConsoleView consoleView;
	private final Display display;
	
	ConsoleLogReader(ConsoleView view) {
		this.consoleView = view;
		this.display = consoleView.getSite().getShell().getDisplay();
	}
	public void log(ILogEntry entry) {
	final OutputState out = new OutputState(entry);
		
		addBanner(out);
		out.println(entry.getMessage());
		addException(out);
		printToConsole(out);
		
	}
	
	public void logRaw(final String message) {
		if(display.isDisposed()) {
			return;
		}
		display.asyncExec(new Runnable() {

			public void run() {
				if(message.endsWith("\n"))
					consoleView.addOutput(message);	
				else
					consoleView.addOutput(message + "\n");
			}
			
		});
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
	
	private void printToConsole(final OutputState out) {
		if(display.isDisposed()) {
			return;
		}
		display.asyncExec(new Runnable() {

			public void run() {
				consoleView.addOutput(out.toString());				
			}
			
		});
	}

}
