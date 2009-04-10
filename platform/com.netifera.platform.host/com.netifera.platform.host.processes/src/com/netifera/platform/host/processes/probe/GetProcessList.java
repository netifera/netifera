package com.netifera.platform.host.processes.probe;

import com.netifera.platform.api.dispatcher.ProbeMessage;
import com.netifera.platform.host.processes.Process;
public class GetProcessList extends ProbeMessage {
	
	private static final long serialVersionUID = -3344277888336347076L;

	public static final String ID = "GetProcessList";

	private final Process[] processes;
	GetProcessList() {
		super(ID);
		processes = null;
	}
	
	GetProcessList createResponse(Process[] processes) {
		return new GetProcessList(processes, getSequenceNumber());
		
	}
	private GetProcessList(Process[] processes, int sequenceNumber) {
		super(ID);
		this.processes = processes;
		setSequenceNumber(sequenceNumber);
		markAsResponse();
	}
	
	Process[] getProcessList() {
		return processes;
	}
}
