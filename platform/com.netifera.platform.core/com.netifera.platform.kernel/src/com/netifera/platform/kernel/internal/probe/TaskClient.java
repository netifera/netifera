package com.netifera.platform.kernel.internal.probe;

import java.util.ArrayList;
import java.util.Collection;

import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tasks.ITaskClient;
import com.netifera.platform.api.tasks.ITaskOutput;
import com.netifera.platform.api.tasks.ITaskRecord;
import com.netifera.platform.api.tasks.ITaskStatus;
import com.netifera.platform.tasks.TaskConsoleOutput;
import com.netifera.platform.tasks.TaskLogOutput;
import com.netifera.platform.tasks.TaskStatus;
import com.netifera.platform.tasks.messages.TaskCancelMessage;
import com.netifera.platform.tasks.messages.TaskListMessage;
import com.netifera.platform.tasks.messages.TaskStartMessage;

public class TaskClient implements ITaskClient {

	final private IProbe probe;
	final private ILogger logger;
	final private IWorkspace workspace;

	private long cachedTaskId = 0;
	private ITaskRecord cachedTaskRecord = null;
	
	public TaskClient(IProbe probe, IWorkspace workspace, ILogger logger) {
		this.probe = probe;
		this.workspace = workspace;
		this.logger = logger.getManager().getLogger("Task Client");
	}

	public void createTask(String instance, long taskId, ISpace space) {
		logger.debug("Creating task with id = " + taskId);
		final ITaskStatus taskStatus = new TaskStatus(instance, taskId);
		space.addTaskRecord(taskStatus);
	}

	public void startTask(long taskId) {
		IMessenger messenger = probe.getMessenger();

		try {
			messenger.sendMessage(new TaskStartMessage(taskId));
		} catch (MessengerException e) {
			logger.warning("Starting task failed", e);
		}
	}

	// message handler
	public void taskChanged(ITaskStatus taskStatus) {
		logger.debug("taskChanged " + taskStatus);
		ITaskRecord record = getTaskForId(taskStatus.getTaskId());
		if(record == null) {
			logger.warning("No task found for id: " + taskStatus.getTaskId());
			return;
		}
		record.updateTaskStatus(taskStatus);
	}

	private ITaskRecord getTaskForId(long taskId) {
		if(cachedTaskId == taskId)
			return cachedTaskRecord;
		
		cachedTaskId = taskId;
		cachedTaskRecord = workspace.findTaskById(taskId);
		return cachedTaskRecord;
	}

	public IProbe getProbe() {
		return probe;
	}

	// message handler
	public void addMessage(ITaskOutput output) {
		logger.debug("Received task output: " + output);
		ITaskRecord record = getTaskForId(output.getTaskId());
		if(record == null) {
			logger.warning("No task found for id: " + output.getTaskId());
			return;
		}
		final long localTaskId = output.getTaskId();
		output.setTaskId(localTaskId);

		if(output instanceof TaskLogOutput) {
			record.addTaskOutput(output);
		} else if(output instanceof TaskConsoleOutput) {
			logger.getManager().logRaw( ((TaskConsoleOutput)output).getMessage() + "\n");
		}

	}

	public ITaskStatus[] getCurrentTasks() {
		IMessenger messenger = getMessenger();
		try {
			TaskListMessage response = (TaskListMessage) messenger.exchangeMessage(new TaskListMessage());
			return response.getTaskList();
		} catch (MessengerException e) {
			logger.warning("Failed sending TaskListMessage", e);
		}
		return new ITaskStatus[0];
	}

	public void requestCancel(long taskId) {
		Collection<Long> taskIdList = new ArrayList<Long>();
		taskIdList.add(taskId);
		requestCancel(taskIdList);
	}


	public void requestCancel(Collection<Long> taskIdList) {
		if(taskIdList == null || taskIdList.isEmpty()) {
			return;
		}

		IMessenger messenger = getMessenger();

		try {
			long taskIdArray[] = new long[taskIdList.size()];
			int i = 0;
			for(Long taskId : taskIdList) {
				taskIdArray[i++] = taskId;
			}
			messenger.emitMessage(new TaskCancelMessage(taskIdArray));
		} catch (MessengerException e) {
			logger.warning("Failed sending TaskCancelMessage", e);
		}

	}

	private IMessenger getMessenger() {
		IMessenger messenger = probe.getMessenger();
		if(messenger == null) {
			throw new IllegalStateException("No messenger in probe (not connected?)");
		}
		return messenger;
	}

}
