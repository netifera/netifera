package com.netifera.platform.host.processes.probe;

import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.dispatcher.StatusMessage;
import com.netifera.platform.host.processes.IProcessManager;
import com.netifera.platform.host.processes.Process;

public class RemoteProcessManager implements IProcessManager {

	private final IProbe probe;
	private final ILogger logger;
	private String messengerError;
	
	public RemoteProcessManager(IProbe probe, ILogger logger) {
		this.probe = probe;
		this.logger = logger;
	}
	
	public Process[] getProcessList() {
		final GetProcessList msg = (GetProcessList) exchangeMessage(new GetProcessList());
		if(msg == null) {
			logger.warning("GetProcessList failed " + messengerError);
			return null;
		}
		return msg.getProcessList();
		
	}

	public boolean kill(int pid) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@SuppressWarnings("unused")
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
