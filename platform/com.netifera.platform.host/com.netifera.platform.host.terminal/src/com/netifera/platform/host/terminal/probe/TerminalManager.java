package com.netifera.platform.host.terminal.probe;

import java.util.HashMap;
import java.util.Map;

import com.netifera.platform.api.dispatcher.DispatchException;
import com.netifera.platform.api.dispatcher.DispatchMismatchException;
import com.netifera.platform.api.dispatcher.IMessageDispatcher;
import com.netifera.platform.api.dispatcher.IMessageDispatcherService;
import com.netifera.platform.api.dispatcher.IMessageHandler;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.host.terminal.IPseudoTerminalFactory;
import com.netifera.platform.host.terminal.ITerminal;
import com.netifera.platform.host.terminal.ITerminalOutputHandler;

public class TerminalManager {
	private ILogger logger;
	private IPseudoTerminalFactory ptyFactory;
	
	private Map<String, ITerminal> ptyMap = new HashMap<String, ITerminal>();
	private Map<String, IMessenger> messengerMap = new HashMap<String, IMessenger>();
	
	private final ITerminalOutputHandler outputHandler;
	
	public TerminalManager() {
		outputHandler = createTerminalOutputHandler();
	}
	
	private ITerminalOutputHandler createTerminalOutputHandler() {
		return new ITerminalOutputHandler() {

			public void terminalOutput(String ptyName, byte[] data, int length) {
				byte[] sendBytes = new byte[length];
				System.arraycopy(data, 0, sendBytes, 0, length);
				
				emitMessage(ptyName, new TerminalOutput(ptyName, sendBytes));
			}

			public void terminalClosed(String ptyName) {
				emitMessage(ptyName, new TerminalClosed(ptyName));	
			}
			
		};
	}
	
	private void openTerminal(IMessenger messenger, OpenTerminal message) {
		final ITerminal terminal = createTerminal(message.getCommand(), messenger);
		if(terminal == null) {
			logger.error("Failed to create terminal");
			return;
		}
		emitMessage(terminal.getName(), message.createResponse(terminal.getName()));
	}
	
	private ITerminal createTerminal(String command, IMessenger messenger) {
		synchronized(messengerMap) {
			final ITerminal terminal = ptyFactory.openTerminal(command, outputHandler);
			if(terminal != null) {
				ptyMap.put(terminal.getName(), terminal);
				messengerMap.put(terminal.getName(), messenger);
			}
			return terminal;
		}
	}
	
	private void terminalInput(IMessenger messenger, TerminalInput message) {
		ITerminal terminal = ptyMap.get(message.getPty());
		if(terminal == null) {
			logger.warning("No terminal found in terminalInput");
			return;
		}
		terminal.sendInput(message.getInput());
	}
	
	private void terminalSizeChange(IMessenger messenger, TerminalSizeChange message) {
		ITerminal terminal = ptyMap.get(message.getPtyName());
		if(terminal == null) {
			logger.warning("No terminal found in terminalSizeChange");
			return;
		}
		terminal.setSize(message.getWidth(), message.getHeight());
	}
	
	private void closeTerminal(IMessenger messenger, CloseTerminal message) {
		closePty(message.getPtyName());
	}
	
	
	private void closePty(String ptyName) {
		synchronized (messengerMap) {
			ITerminal terminal = ptyMap.get(ptyName);
			if(terminal != null) {
				terminal.close();
			}
			ptyMap.remove(ptyName);
			messengerMap.remove(ptyName);
		}
	}
	private void emitMessage(String ptyName, IProbeMessage message) {
		IMessenger messenger;
		synchronized(messengerMap) {
			messenger = messengerMap.get(ptyName);
		}
		
		if(messenger == null) {
			logger.error("Cannot send message because probe has no active messenger");
			closePty(ptyName);
			return;
		}
		
		try {
			messenger.emitMessage(message);
		} catch (MessengerException e) {
			logger.warning("Messenger error sending message " + e.getMessage(), e);
			closePty(ptyName);
		}
	}
	
	private void registerHandlers(IMessageDispatcher dispatcher) {
		IMessageHandler handler = new IMessageHandler() {

			public void call(IMessenger messenger, IProbeMessage message)
					throws DispatchException {
				if(message instanceof OpenTerminal) {
					openTerminal(messenger, (OpenTerminal) message);					
				} else if(message instanceof TerminalInput) {
					terminalInput(messenger, (TerminalInput) message);
				} else if(message instanceof TerminalSizeChange) {
					terminalSizeChange(messenger, (TerminalSizeChange) message);
				} else if(message instanceof CloseTerminal) {
					closeTerminal(messenger, (CloseTerminal) message);
				} else {
					throw new DispatchMismatchException(message);
				}
			}
		};
		
		dispatcher.registerMessageHandler(OpenTerminal.ID, handler);
		dispatcher.registerMessageHandler(TerminalInput.ID, handler);
		dispatcher.registerMessageHandler(TerminalSizeChange.ID, handler);
		dispatcher.registerMessageHandler(CloseTerminal.ID, handler);
	}
	protected void setMessageDispatcher(IMessageDispatcherService dispatcher) {
		registerHandlers(dispatcher.getServerDispatcher());
	}
	
	protected void unsetMessageDispatcher(IMessageDispatcherService dispatcher) {
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Terminal Manager");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}
	
	protected void setPtyFactory(IPseudoTerminalFactory factory) {
		ptyFactory = factory;
	}
	
	protected void unsetPtyFactory(IPseudoTerminalFactory factory) {
		
	}
}
