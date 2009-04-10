package com.netifera.platform.ui.updater;

import java.util.concurrent.ScheduledFuture;

/**
 * ScheduledTask implementation to use with TaskScheduler for the UI updater. It is generic
 * and does not have UI specific code so it could be probably be replaced for some standard 
 * implementation.
 * 
 * @author kevin
 *
 */
public class ScheduledTask implements Runnable {
	private long lastRunEnd;
	private volatile boolean scheduled;
	private volatile boolean cancelled;
	/* period and delay unit is milliseconds */
	protected int period;
	protected int delay;
	protected long nextRunStart;
	protected long lastRunStart;
	protected final TaskScheduler taskScheduler = TaskScheduler.getInstance();
	protected final Runnable task;
	private ScheduledFuture<?> future;

	public ScheduledTask(Runnable task) {
		this.task = task;
	}

	/**
	 * @param task Runnable
	 * @param delay milliseconds to wait before running the task again
	 */

	public ScheduledTask(Runnable task, int period, int delay) {
		this.task = task;
		this.delay = delay;
		this.period = period;
	}

	public void run() {

		if (cancelled) {
			return;
		}
		/* if the task is periodic it is always scheduled */
		if (period == 0) {
			scheduled = false;
		}

		lastRunEnd = 0;
		nextRunStart = 0;
		lastRunStart = System.currentTimeMillis();

		if (period >= 0) {
			nextRunStart = lastRunStart + period;
		}

		task.run();

		lastRunEnd = System.currentTimeMillis();

		if (delay >= 0) {
			nextRunStart = lastRunEnd + delay;
		}

	}

	public void schedule() {
		if (scheduled) {
			return;
		}
		/* double-checked locking broken in java 1.4 and earlier */
		synchronized (this) {
			if (!scheduled) {
				scheduled = true;
				future = taskScheduler.submit(this);
			}
		}
	}

	private void reschedule() {
		taskScheduler.remove(this);
		scheduled = true;
		taskScheduler.submit(this);
	}

	public void cancel() {
		cancelled = true;
		taskScheduler.remove(this);
	}

	public int getNextRunDelay() {
		long nextDelay = nextRunStart - System.currentTimeMillis();
		return nextDelay > 0 ? (int) nextDelay : 0;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public synchronized int getPeriod() {
		return period;
	}

	public synchronized void setPeriod(int period) {
		this.period = period;
		reschedule();
	}

	public int hashCode() {
		return task.hashCode();
	}

	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (!(other instanceof ScheduledTask)) {
			return false;
		}

		return (((ScheduledTask) other).task == this.task);
	}

	public void setFuture(ScheduledFuture<?> future) {
		this.future = future;
	}

	public ScheduledFuture<?> getFuture() {
		return future;
	}
}
