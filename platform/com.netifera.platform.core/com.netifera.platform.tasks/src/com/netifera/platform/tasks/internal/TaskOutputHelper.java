package com.netifera.platform.tasks.internal;

import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.dispatcher.ProbeMessage;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.tasks.ITaskOutput;
import com.netifera.platform.api.tasks.ITaskStatus;
import com.netifera.platform.api.tasks.TaskOutputMessage;
import com.netifera.platform.tasks.messages.TaskChangedMessage;

public class TaskOutputHelper {
	
	private IMessenger messenger;
	private final Task task;
	private final ILogger logger;
	
	TaskOutputHelper(Task task, IMessenger messenger, ILogger logger) {
		this.task = task;
		this.messenger = messenger;
		this.logger = logger;
	}
	
	private final List<ITaskOutput> pendingOutput = new ArrayList<ITaskOutput>();
	private boolean changePending;

	synchronized IMessenger getMessenger() {
		return messenger;
	}
	
	void changed() {
		if(messenger == null) {
			return;
		}
		final TaskChangedMessage message = new TaskChangedMessage(
				new ITaskStatus[] { task.getStatus() });
		
		if(!transmitMessage(message)) {
			changePending = true;
		}
	}
	
	synchronized void setMessenger(IMessenger newMessenger) {
		/* first send pending output if any */
		if(!pendingOutput.isEmpty()) {
			TaskOutputMessage outputMessage = new TaskOutputMessage();
			/* send all the output in a single message */
			for(ITaskOutput output : pendingOutput) {
				outputMessage.addOutput(output);
			}
			/* If the message is sent successfully pending output are removed.
			 * Otherwise messenger will be null after returning from transmitMessage */
			messenger = newMessenger;
			if(transmitMessage(outputMessage)) {
				pendingOutput.clear();
			} 
		}
		if(changePending) {
			changed();
			changePending = false;
		}
	}
	
	void addMessage(ITaskOutput output) {
		if (!output.isInitialized()) {
			output.initialize(task.getStatus().getTaskId());
		}
		
		if(!transmitMessage(new TaskOutputMessage(output))) {
			storeOutput(output);
		}
	}
	private synchronized boolean transmitMessage(ProbeMessage message) {

		if (messenger == null) {
			/* can't send, store. */
			return false;
		}

		try {
			logger.debug("Sending ProbeMessage: " + message);
			messenger.emitMessage(message);
			return true;

		} catch (MessengerException e) {
			logger.warning("Send ProbeMessage failed", e);
			/* can't send, store. */
			messenger = null;
			return false;
		}
	}
	
	private void storeOutput(ITaskOutput output) {
		/* commands are stored if they can not be sent. A queue could be used here. */
		pendingOutput.add(output);
	}
}
