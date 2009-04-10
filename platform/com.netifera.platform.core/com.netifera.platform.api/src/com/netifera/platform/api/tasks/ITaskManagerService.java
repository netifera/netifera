package com.netifera.platform.api.tasks;

import com.netifera.platform.api.dispatcher.IMessenger;

public interface ITaskManagerService {
	ITask createTask(ITaskRunnable runnable, IMessenger messenger);

}
