package com.netifera.platform.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.db4o.ext.DatabaseClosedException;
import com.netifera.platform.api.events.EventListenerManager;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.tasks.ITaskOutput;
import com.netifera.platform.api.tasks.ITaskOutputEvent;
import com.netifera.platform.api.tasks.ITaskRecord;
import com.netifera.platform.api.tasks.ITaskStatus;

public class TaskRecord implements ITaskRecord {
	private final static int BACKGROUND_COMMIT_INTERVAL = 30000;
	private final Space space;
	private final ITaskStatus taskStatus;
	private final List<ITaskOutput> taskOutput;
	private final long taskId;
	private transient EventListenerManager taskChangeListeners;
	private transient Thread commitThread;
	private transient volatile boolean commitThreadActive;
	private transient volatile boolean taskOutputDirty;
	
	TaskRecord(ITaskStatus status, Space space) {
		this.space = space;
		this.taskStatus = status;
		this.taskOutput = new ArrayList<ITaskOutput>();
		this.taskId = status.getTaskId();
		space.getDatabase().store(this);
		this.commitThreadActive = false;
	}

	public void updateTaskStatus(ITaskStatus newStatus) {
		taskStatus.update(newStatus);
		space.getDatabase().store(taskStatus);
		space.updateTaskRecord(this);
		if(commitThreadActive && (taskStatus.isFinished() || taskStatus.isFailed())) {
			stopCommitThread();
		}
	}
	public void addTaskOutputListener(IEventHandler handler) {
		getEventManager().addListener(handler);
	}
	
	public void removeTaskOutputListener(IEventHandler handler) {
		getEventManager().removeListener(handler);
	}
	
	private EventListenerManager getEventManager() {
		if(taskChangeListeners == null) {
			taskChangeListeners = new EventListenerManager();
		}
		return taskChangeListeners;
	}
	
	public List<ITaskOutput> getTaskOutput() {
		return Collections.unmodifiableList(taskOutput);
	}
	
	public void addTaskOutput(final ITaskOutput output) {
		taskOutput.add(output);
		taskOutputDirty = true;
		if(!commitThreadActive)
			startCommitThread();
		space.updateTaskRecord(this);
		getEventManager().fireEvent(new ITaskOutputEvent() {
			public ITaskOutput getMessage() {
				return output;
			}
		});
	}
	
	/* Delegates to taskStatus */
	public long getElapsedTime() {
		return taskStatus.getElapsedTime();
	}

	public long getProbeId() {
		return space.getProbeId();
	}

	public long getStartTime() {
		return taskStatus.getStartTime();
	}

	public String getStateDescription() {
		return taskStatus.getStateDescription();
	}

	/* Must not delegate to taskStatus or it will break the query optimizer */
	public long getTaskId() {
		return taskId;
	}

	public String getTitle() {
		return taskStatus.getTitle();
	}

	public String getStatus() {
		return taskStatus.getStatus();
	}
	
	public int getWorkDone() {
		return taskStatus.getWorkDone();
	}
	
	public int getRunState() {
		return taskStatus.getRunState();
	}
	
	public boolean isFailed() {
		return taskStatus.isFailed();
	}

	public boolean isFinished() {
		return taskStatus.isFinished();
	}

	public boolean isRunning() {
		return taskStatus.isRunning();
	}

	public boolean isWaiting() {
		return taskStatus.isWaiting();
	}	
	
	private synchronized void startCommitThread() {
		if(commitThreadActive) {
			return;
		}
		commitThread = new Thread(new Runnable() {

			public void run() {
				while(commitThreadActive) {
					try {
						Thread.sleep(BACKGROUND_COMMIT_INTERVAL);
						if(space.getDatabase().ext().isClosed()) {
							commitThreadActive = false;
							return;
						} else {
							runCommit();
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						commitThreadActive = false;
					} catch(DatabaseClosedException e) {
						commitThreadActive = false;
						return;
					}
					
				}
				runCommit();
				return;	
			}
			
		});
		commitThread.setDaemon(true);
		if(getTitle() != null) {
			commitThread.setName("Background Commit TaskRecord [" + getTitle() + "]");
		} else {
			commitThread.setName("Background Commit TaskRecord [taskId = " + taskId + "]");
		}
		commitThreadActive = true;
		commitThread.start();
	}
	
	private synchronized void stopCommitThread() {
		if(!commitThreadActive) {
			return;
		}
		commitThreadActive = false;
		commitThread.interrupt();
//		try {
//			commitThread.join();
//		} catch (InterruptedException e) {
//			Thread.currentThread().interrupt();
//		}
		commitThread = null;
	}
	
	private synchronized void runCommit() {
		if(!taskOutputDirty) 
			return;
		
		synchronized(taskOutput) {
			space.getDatabase().store(taskOutput);
		}
		
	}

}
