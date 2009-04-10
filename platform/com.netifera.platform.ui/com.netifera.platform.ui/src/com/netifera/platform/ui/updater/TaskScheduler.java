package com.netifera.platform.ui.updater;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskScheduler {
	private static final TaskScheduler instance = new TaskScheduler();

	private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

	private TaskScheduler() {
		/* we want only one thread */
		executor.setMaximumPoolSize(1); // not useful since ScheduledThreadPoolExecutor has PoolSize fixed
	}

	public static TaskScheduler getInstance() {
		return instance;
	}

	public void remove(ScheduledTask scheduledTask) {
		ScheduledFuture<?> future = scheduledTask.getFuture();
		if(future != null) {
			future.cancel(false);
			executor.purge();
		}
	}

	public ScheduledFuture<?> submit(ScheduledTask scheduledTask) {
		int period = scheduledTask.getPeriod();
		if(period > 0) {
			return executor.scheduleAtFixedRate(scheduledTask, scheduledTask.getNextRunDelay(),scheduledTask.getPeriod(),TimeUnit.MILLISECONDS);
		}
		else {
			return executor.schedule(scheduledTask, scheduledTask.getNextRunDelay(), TimeUnit.MILLISECONDS);
		}
	}
}
