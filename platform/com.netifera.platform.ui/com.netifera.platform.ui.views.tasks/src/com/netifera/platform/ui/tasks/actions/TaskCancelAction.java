/**
 * 
 */
package com.netifera.platform.ui.tasks.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tasks.ITaskClient;
import com.netifera.platform.api.tasks.ITaskRecord;
import com.netifera.platform.ui.internal.tasks.TasksPlugin;

/**
 * @author kevin
 *
 */
public class TaskCancelAction extends Action {
	private static final String STOP_TASK_IMAGE = "icons/stop_task.png";
	private ISelectionProvider selectionProvider;
	private final ITaskRecord taskRecord;

	/* set the listener to change action state based on selection */
	private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			setEnabled(shouldEnable(selection));
		}
	};

	public TaskCancelAction(ISelectionProvider selectionProvider) {
		setSelectionProvider(selectionProvider);
		taskRecord = null;
		setLabel();

	}
	public TaskCancelAction(ITaskRecord taskRecord) {
		this.taskRecord = taskRecord;
		this.selectionProvider = null;
		setLabel();
	}
	private void setLabel() {
		setEnabled(shouldEnable());
		setId("cancelTask");
		setText("Cancel task");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(TasksPlugin.PLUGIN_ID, STOP_TASK_IMAGE));
	}

	public void setSelectionProvider(ISelectionProvider selectionProvider) {
		if(this.selectionProvider != null) {
			this.selectionProvider.removeSelectionChangedListener(selectionListener);
		}
		this.selectionProvider = selectionProvider;
		selectionProvider.addSelectionChangedListener(selectionListener);
	}

	private boolean shouldEnable() {

		if(taskRecord != null) {
			return shouldEnable(taskRecord);
		}

		if(selectionProvider != null) {
			return shouldEnable(selectionProvider.getSelection());
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private boolean shouldEnable(ISelection selection) {
		boolean enabled = false;
		if(selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection)selection;
			if(!sel.isEmpty()) {
				enabled = true;
				for(ITaskRecord taskRecord : (List<ITaskRecord>)sel.toList()) {
					/* enable if every selected task satisfy this condition */
					enabled &= shouldEnable(taskRecord);
				}
			}
		}
		return enabled;
	}

	private boolean shouldEnable(ITaskRecord taskRecord) {
	    return !taskRecord.isFinished() && !taskRecord.isFailed();
	}

	@Override
    public void run() {
		if (taskRecord != null) {
			getTaskManager(taskRecord.getProbeId()).requestCancel(taskRecord.getTaskId());
		} else if (selectionProvider != null) {
			cancelSelection((IStructuredSelection) selectionProvider.getSelection());
		}
		/* if the cancel was requested the button is disabled, it could be enabled again
		 * if the selection changes. If taskRecord is null and there is no selectionProvider
		 * the button will be disabled for ever but it was not useful anyway.*/
		setEnabled(false);
	}

	@SuppressWarnings("unchecked")
	private void cancelSelection(IStructuredSelection selection) {
		/* TODO find a better way to get the task manager than this probeId long */
		long probeId = 0;
		/*create list of task id from selection */
		List<Long> taskIdList = new ArrayList<Long>();
		for(ITaskRecord taskRecord : (List<ITaskRecord>)selection.toList()) {
			taskIdList.add(taskRecord.getTaskId());
			probeId = taskRecord.getProbeId();
		}

		if(!taskIdList.isEmpty()) {
			getTaskManager(probeId).requestCancel(taskIdList);
		}
	}

	private ITaskClient getTaskManager(long probeId) {
		IProbe probe = TasksPlugin.getPlugin().getProbeManager().getProbeById(probeId);
		return probe.getTaskClient();
	}
}
