package com.netifera.platform.api.tasks;

import java.util.Collection;
import java.util.List;

import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.probe.IProbe;

/**
 * Client of the probe task manager.
 */
public interface ITaskClient {

	
	void createTask(String instanceName, long taskId, ISpace space);
	void startTask(long taskId);

	/**
	 * @return array of ITaskStatus of all running, scheduled and finished tasks.
	 */
	 ITaskStatus[] getCurrentTasks();

	//ITaskStatus getTaskRecord(long taskId);

	/**
	 * Requests the cancellation of the given task id.
	 * 
	 * @param taskId
	 */
	void requestCancel(long taskId);

	/**
	 * Requests the cancellation of every task id in the collection.
	 * @param taskIdList
	 */
	void requestCancel(Collection<Long> taskIdList);

	/**
	 * Send a request to cancel all running tasks with the given class name.
	 * 
	 * @param className The class name to cancel all instances of
	 */
	//void requestCancelAllByClass(String className);
	
	/**
	 * return true if the tool described by the named class is already running
	 * @param className Name of the class for the tool
	 * @return True if running, false otherwise
	 */
	//boolean isClassRunning(String className);

	void taskChanged(ITaskStatus record);

	void addMessage(ITaskOutput taskMessage);
	
	//List<ITaskOutput> getMessageListFor(long taskId);
	
	/**
	 * return the probe this task manager is associated with
	 * @return
	 */
	IProbe getProbe();

//	void addTaskOutputListener(IEventHandler handler);
//	void removeTaskOutputListener(IEventHandler handler);
//	
//	void addTaskChangeListener(IEventHandler handler);
//	
//	void removeTaskChangeListener(IEventHandler handler);
}
