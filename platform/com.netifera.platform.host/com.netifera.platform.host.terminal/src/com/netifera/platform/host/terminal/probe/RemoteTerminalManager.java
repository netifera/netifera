package com.netifera.platform.host.terminal.probe;

import java.util.HashMap;
import java.util.Map;

import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.dispatcher.StatusMessage;
import com.netifera.platform.host.terminal.ITerminal;
import com.netifera.platform.host.terminal.ITerminalManager;
import com.netifera.platform.host.terminal.ITerminalOutputHandler;

public class RemoteTerminalManager implements ITerminalManager {
	
	private final ILogger logger;
	private final IProbe probe;
	private String messengerError;
	private Map<String, RemoteTerminal> ptyMap = new HashMap<String, RemoteTerminal>();
	
	RemoteTerminalManager(IProbe probe, ILogger logger) {
		this.probe = probe;
		this.logger = logger;
	}
	
	public ITerminal openTerminal(String command, ITerminalOutputHandler outputHandler) {
		OpenTerminal response = (OpenTerminal) exchangeMessage(new OpenTerminal(command));
		if(response == null) {
			logger.error("Error sending open terminal message " + messengerError);
			return null;
		}
		
		RemoteTerminal terminal = new RemoteTerminal(response.getPty(), this, outputHandler);
		ptyMap.put(terminal.getName(), terminal);
		return terminal;
		
	}
	
	void terminalOutput(TerminalOutput terminalOutput) {
		RemoteTerminal terminal = ptyMap.get(terminalOutput.getPty());
		terminal.receiveOutput(terminalOutput.getOutput());
		
	}
	
	void terminalClosed(TerminalClosed terminalClosed) {
		RemoteTerminal terminal = ptyMap.get(terminalClosed.getPtyName());
		
		if(terminal != null) {
			terminal.setClosed();
		}
	}
	
	void terminalInput(String pty, byte[] input) {
		if(!sendMessage(new TerminalInput(pty ,input))) {
			logger.warning("Failed sending terminal input message " + messengerError);
		}
	}

	IProbe getProbe() {
		return probe;
	}
	
	ILogger getLogger() {
		return logger;
	}
	
	void removeTerminal(String ptyName) {
		ptyMap.remove(ptyName);
	}
	
	private boolean sendMessage(IProbeMessage message) {
		try {
			probe.getMessenger().sendMessage(message);
			return true;
		} catch (MessengerException e) {
			messengerError = e.getMessage();
			return false;
		}
	}
	
	private IProbeMessage exchangeMessage(IProbeMessage message) {
		
		try {
			IProbeMessage response = probe.getMessenger().exchangeMessage(message);
			if(response instanceof StatusMessage) { 
				return null;
			} else {
				return response;
			}
		} catch (MessengerException e) {
			messengerError = e.getMessage();
			return null;
		}
	}
}
