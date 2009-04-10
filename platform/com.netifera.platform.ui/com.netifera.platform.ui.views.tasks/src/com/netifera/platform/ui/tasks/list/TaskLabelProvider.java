package com.netifera.platform.ui.tasks.list;

import java.text.DateFormat;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.netifera.platform.api.tasks.ITaskRecord;
import com.netifera.platform.ui.images.ImageCache;
import com.netifera.platform.ui.internal.tasks.TasksPlugin;

public class TaskLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	private static final String TASK_WAITING_IMAGE = "icons/ledyellow_16x16.png";
	private static final String TASK_RUNNING_IMAGE = "icons/ledgreen_16x16.png";
	private static final String TASK_FINISHED_IMAGE = "icons/ledoff_16x16.png";
	private static final String TASK_FAILED_IMAGE = "icons/ledred_16x16.png";
	
	static final int SECONDS = 1000;
	static final int MINUTES = SECONDS * 60;
	static final int HOURS = MINUTES * 60;
	static final int DAYS = HOURS * 24;
	
	@Override
	public String getText(Object element) {
		if (!(element instanceof ITaskRecord)) {
			return element.toString();
		}
		ITaskRecord taskRecord = (ITaskRecord) element;
		return getTaskName(taskRecord);
	}

	@Override
	public Image getImage(Object element) {
		if (!(element instanceof ITaskRecord)) {
			return null;
		}
		ITaskRecord taskRecord = (ITaskRecord) element;
		ImageCache images = TasksPlugin.getPlugin().getImageCache();
		if (taskRecord.isWaiting()) {
			return images.get(TASK_WAITING_IMAGE);
		} else if (taskRecord.isRunning()) {
			return images.get(TASK_RUNNING_IMAGE);
		} else if (taskRecord.isFinished()) {
			return images.get(TASK_FINISHED_IMAGE);
		}
		return images.get(TASK_FAILED_IMAGE);
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return (columnIndex != 0) ? null : getImage(element);
	}

	/**
	 * Return text string to display for given element in the indicated column
	 */
	public String getColumnText(Object element, int columnIndex) {
		if ((element instanceof ITaskRecord) == false) {
			return "??";
		}
		ITaskRecord tr = (ITaskRecord) element;

		switch (columnIndex) {
		case 0:
			return "";
		case 1: /* label */
			return getText(element);
		case 2: /* start time */
			return getStartTime(tr);
		case 3: /* elapsed time */
			return getElapsedTime(tr);
		default:
			return "? " + columnIndex;
		}
	}

	public String getTaskName(ITaskRecord taskRecord) {
		//TODO should use the action name
		String name = taskRecord.getTitle();
		return name == null ? "Untitled task" : name;
	}
	
	/**
	 * Content description for tasks. A short text describing the selected task and
	 * its status. For example:
	 * <status> title [tool name ] summary of the input (time reference) (short task id)
	 * @param taskRecord a TaskRecord
	 */
	public String getDescription(ITaskRecord taskRecord) {
		return "<" + getState(taskRecord) + "> " + getTaskName(taskRecord) + " (" + getStartTime(taskRecord) + ")"; 
	}
	
	public String getStartTime(ITaskRecord taskRecord) {
		long startTime = taskRecord.getStartTime();
		if(startTime != 0) {
			/*
			 * KLUDGE: thread-safe DateFormat
			 * 
			 * creates a new instance of a Format object for each invocation
			 * (performance hit)
			 */
			return DateFormat.getInstance().format(startTime);
		}
		/*start time not set and status is not waiting then the task never run*/
		return taskRecord.isWaiting() ?	"Waiting ..." : "Never";
	}
	
	/**
	 * @param taskRecord
	 * @return English word representing the task status
	 */
	public String getState(ITaskRecord taskRecord) {
		return taskRecord.getStateDescription();
	}

	public String getElapsedTime(ITaskRecord taskRecord) {
		long millis = taskRecord.getElapsedTime();
		if (millis == 0) {
			return "00:00:00";
		}
		StringBuffer sb = new StringBuffer();
		if (millis < 0) {
			sb.append('-');
			millis = -millis;
		}
		if(millis < SECONDS) {
			sb.append(millis);
			sb.append(" milliseconds");
			return sb.toString();
		}
		if(millis < MINUTES) {
			sb.append(millis / SECONDS);
			sb.append(" seconds");
			return sb.toString();
		}
		
		long day = millis / DAYS;

		if (day != 0) {
			sb.append(day);
			sb.append(" days ");
			millis = millis % DAYS;
		}

		long hours = millis / HOURS;
		millis = millis % HOURS;
		
		long minutes = millis / MINUTES;
		millis = millis % MINUTES;
		
		long seconds = millis / SECONDS;
		millis = millis % SECONDS;

		if(hours < 10)
			sb.append('0');
		sb.append(hours);
		
		sb.append(':');
		if(minutes < 10)
			sb.append('0');
		sb.append(minutes);
		
		sb.append(':');
		if(seconds < 10)
			sb.append('0');
		sb.append(seconds);
		
		return sb.toString();
	}

}
