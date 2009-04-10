package com.netifera.platform.tasks.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.api.tasks.ITask;
import com.netifera.platform.api.tasks.ITaskManagerService;
import com.netifera.platform.api.tasks.ITaskStatus;
import com.netifera.platform.api.tasks.ITaskRunnable;
import com.netifera.platform.tasks.TaskStatus;
import com.netifera.platform.tasks.messages.TaskCancelMessage;
import com.netifera.platform.tasks.messages.TaskListMessage;
import com.netifera.platform.tasks.messages.TaskStartMessage;
import com.netifera.platform.tasks.messages.TasksSubscribeMessage;


public class TaskManager implements ITaskManagerService {

	private static final ThreadPoolExecutor exec = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
	private static final Map<Long,Task> tasks = new HashMap<Long,Task>();
	private IModelService model;
	private ILogger logger;

	/*
	 * TaskCancelMessage    --> taskCancelHandler
	 * TaskListMessage      --> taskListHandler
	 * TaskSubscribeMessage --> subscribeToTasks
	 */
	private void registerHandlers(IMessageDispatcher dispatcher) {

		IMessageHandler handler = new IMessageHandler() {
			public void call(IMessenger messenger, IProbeMessage message)
					throws DispatchException {
				if(message instanceof TaskCancelMessage) {
					taskCancelHandler((TaskCancelMessage) message);
				} else if(message instanceof TaskListMessage) {
					taskListHandler(messenger, (TaskListMessage) message);
				} else if(message instanceof TasksSubscribeMessage) {
					subscribeToTasks( ((TasksSubscribeMessage)message).getTaskList(), messenger);
				} else if(message instanceof TaskStartMessage) {
					startTask(messenger, (TaskStartMessage)message);
				} else {
					throw new DispatchMismatchException(message);
				}				
			}
			
		};
		
		dispatcher.registerMessageHandler(TaskCancelMessage.ID, handler);
		dispatcher.registerMessageHandler(TaskListMessage.ID, handler);		
		dispatcher.registerMessageHandler(TasksSubscribeMessage.ID, handler);
		dispatcher.registerMessageHandler(TaskStartMessage.ID, handler);
	}


	public ITask createTask(ITaskRunnable runnable, IMessenger messenger) {
		synchronized(this) {
			long newTaskId = model.getCurrentWorkspace().generateTaskId();
			TaskStatus record = new TaskStatus(runnable.getClassName(), newTaskId);
			Task task = new Task(record, runnable, messenger, this, logger);
			tasks.put(newTaskId,task);
			return task;
		}
	}

	void runTask(Task t) {
		exec.execute(t);
	}


	private void taskCancelHandler(TaskCancelMessage message) {
		long taskIdList[] = message.getTaskList();

		if(taskIdList == null) {
			return;
		}

		for(long taskId : taskIdList) {
			if(tasks.containsKey(taskId)) {
				Task task = tasks.get(taskId);
				if(task.getStatus().isWaiting()) {
					synchronized(tasks) {
						exec.remove(task);
						 task.cancel();
						tasks.remove(taskId);
					}
				} else if(task.getStatus().isRunning()) {
					task.cancel();
				}
			}
		}
	}
	
	private void taskListHandler(IMessenger messenger, TaskListMessage message) {
		synchronized(tasks) {
			ITaskStatus[] tasksArray = new ITaskStatus[tasks.size()];
			int i = 0;
			for(Task t : tasks.values()) {
				tasksArray[i] = t.getStatus();
				i++;
			}
			sendResponse(messenger, message.createResponse(tasksArray));
		}
	}
	private void sendResponse(IMessenger messenger, IProbeMessage message) {
		try {
			messenger.emitMessage(message);
		} catch (MessengerException e) {
			logger.error("Failed to send message response", e);
		}
	}
	private void subscribeToTasks(List<Long> tasksIds, IMessenger messenger) {
		/* if list null or empty subscribe to every task */
		if(tasksIds == null || tasksIds.size() == 0) {
			for(Task task : tasks.values()) {
				task.setMessenger(messenger);
			}
		} else {

			for(Long taskId : tasksIds) {
				if(tasks.containsKey(taskId)) {
					tasks.get(taskId).setMessenger(messenger);
				}
			}
		}
	}

	private void startTask(IMessenger messenger, TaskStartMessage message) {
		final long id = message.getTaskId();
		final Task task = tasks.get(id);
		if(task == null) {
			sendError(messenger, message, "No such task exists");
			return;
		}
		
		if(task.isStarted()) {
			sendError(messenger, message, "Task is already started");
			return;
		}
		sendOk(messenger, message);	
		task.start();
	}
	
	private void sendError(IMessenger messenger, IProbeMessage message, String errorMsg) {
		try {
			messenger.respondError(message, errorMsg);
		} catch (MessengerException e) {
			logger.warning("Failed to send error response", e);
		}
	}
	
	private void sendOk(IMessenger messenger, IProbeMessage message) {
		try {
			messenger.respondOk(message);
		} catch (MessengerException e) {
			logger.warning("Failed to send Ok response", e);
		}
	}
	// OSGi DS
	
	protected void setDispatcher(IMessageDispatcherService dispatcherService) {
		registerHandlers(dispatcherService.getServerDispatcher());
	}

	protected void unsetDispatcher(IMessageDispatcherService dispatcherService) {
		
	}

	protected void setModelService(IModelService model) {
		this.model = model;
	}
	protected void unsetModelService(IModelService model) {
		
	}
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Task Manager");
	}

	protected void unsetLogManager(ILogManager logManager) {
		logger = null;
	}
	
}