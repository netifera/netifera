package com.netifera.platform.tasks.internal;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.tasks.ITask;
import com.netifera.platform.api.tasks.ITaskMessenger;
import com.netifera.platform.api.tasks.ITaskOutput;
import com.netifera.platform.api.tasks.ITaskRunnable;
import com.netifera.platform.api.tasks.TaskException;
import com.netifera.platform.tasks.ITaskProgress;
import com.netifera.platform.tasks.ITaskPrompter;
import com.netifera.platform.tasks.TaskConsoleOutput;
import com.netifera.platform.tasks.TaskLogOutput;
import com.netifera.platform.tasks.TaskStatus;

public class Task implements Runnable, ITaskProgress, ITaskPrompter, ITaskMessenger, ITask {
	
	private final ITaskRunnable runnableInstance;
	
	private Thread taskThread;
		
	/* Not used right now */
	private ITaskPrompter prompter;
		
	private final TaskManager taskManager;
	
	private final TaskStatus status;

	private volatile boolean canceled;

	
	private boolean started;
	
	private final ILogger logger;
	private final TaskProgressHelper progress;
	private final TaskOutputHelper output;

	
	
	Task(TaskStatus record, ITaskRunnable instance, IMessenger messenger, 
			TaskManager taskManager, ILogger logger) {
		assert instance != null;
		assert messenger != null;
		this.status = record;
		this.runnableInstance = instance;
		this.taskManager = taskManager;
		this.logger = logger;
		output = new TaskOutputHelper(this, messenger, logger);
		progress = new TaskProgressHelper(this, output);
	}

	public void start() {
		if(!started) {
			started = true;
			taskManager.runTask(this);
		}
	}
	
	public void run() {
		taskThread = Thread.currentThread();
		try {
			status.setRunning();
			status.setStartTime(System.currentTimeMillis());
			output.changed();
			
			/* run the tool */
			runnableInstance.run(this);
			
			status.setFinished();
			output.changed();
		
		} catch(TaskException e) {
			String message = e.getMessage();
			if (message == null) message = e.toString();
			error(message);
			status.setFailed();
			output.changed();
		} catch (Exception e) {
			logger.warning("Unhandled exception in task running tool: " + this, e);
			error("Unexpected error, please contact technical support.\n" + e);
			status.setFailed();
			output.changed();
		}
	}
	
	public void cancel() {
		canceled = true;
		if(taskThread != null) {
			taskThread.interrupt();
		} else {
			/*to trigger update in the client when scheduled tasks are canceled */
			status.setFinished();
			output.changed();
		}
	}
	
	public void failed() {
		logger.debug("Task failed: " + this);
		status.setFailed();
		/* Fill the progress bar. RunState is not changed on purpose */
		status.updateElapsedTime();
		output.changed();
	}
	
	boolean isStarted() {
		return started;
	}
	/** Running Task methods */

	public long getTaskId() {
		return status.getTaskId();
	}

//	public long getProbeId() {
//		return record.getProbeId();
//	}

	@Override
    public String toString() {
		return status.getTitle();
	}

	public TaskStatus getStatus() {
		return status;
	}
	
	public IMessenger getMessenger() {
		return output.getMessenger();
	}
	public void setMessenger(IMessenger messenger) {
		output.setMessenger(messenger);
	}

	public void addMessage(ITaskOutput taskOutput) {
		output.addMessage(taskOutput);
	}

	/** ITaskProgress interface, could be delegated  */

	public void setTotalWork(int totalWork) {
		progress.setTotalWork(totalWork);
	}

	public void worked(int work) {
		progress.worked(work);
	}

	public void done() {
		progress.done();
	}

	public boolean isCanceled() {
		if(taskThread != null && taskThread.isInterrupted()) {
			canceled = true;
		}
		return canceled;
	}

	public void setTitle(String title) {
		this.status.setTitle(title);
		output.changed();
	}
	
	public void setStatus(String status) {
		this.status.setStatus(status);
		output.changed();
	}
	
	/* XXX Task Logging methods could be implemented in ITaskLogging interface and delegate*/

	
	public void debug(String message) {
		log(TaskLogOutput.DEBUG, message);
	}

	public void info(String message) {
		log(TaskLogOutput.INFO, message);
	}

	public void warning(String message) {
		log(TaskLogOutput.WARNING, message);
	}

	public void error(String message) {
		log(TaskLogOutput.ERROR, message);
	}

	public void log(int logLevel, String message) {
		addMessage(new TaskLogOutput(logLevel, message));
	}
	
	public void print(String message) {
		addMessage(new TaskConsoleOutput(message));
	}
	
	public void exception(String message, Throwable throwable) {
		final String output = message + "\n" + renderBacktrace(throwable);
		addMessage(new TaskConsoleOutput(output));
	}
	
	private String renderBacktrace(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		if(throwable.getMessage() != null) {
			pw.println(throwable.getMessage());
		}
		throwable.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}
	
	public String askPassword(String message) {
		if(prompter == null) {
			return null;
		}
		
		return prompter.askPassword(message);
	}

	public String askString(String message) {
		if(prompter == null) {
			return null;
		}
		
		return prompter.askString(message);
	}
}
