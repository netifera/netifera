package com.netifera.platform.ui.application.workspaces;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tasks.ITaskClient;
import com.netifera.platform.api.tasks.ITaskStatus;
import com.netifera.platform.ui.application.ApplicationPlugin;

/**
 * Close workspace handler extends AbstractHandler, an IHandler base class.
 * This handler is set to the Exit menu in ui.application/plugin.xml
 * and is invoked from preWindowShellClose in ApplicationWorkbenchWindowAdvisor 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class CloseWorkspaceHandler extends AbstractHandler {
	private IWorkbenchWindow activeWorkbenchWindow;

	public CloseWorkspaceHandler() {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		if (activeWorkbenchWindow == null) {
			// action has been disposed
			return Boolean.FALSE;
		}
		
		/* code to execute before closing follows */
		
		/* get the list of running tasks in the local probe */
		List<Long> runningTasksList = getRunningTasksList();

		/* if local probe is running tasks ask the user for confirmation */
		Boolean confirm = showConfirmDialog(runningTasksList);
		
		if(confirm) {
			
			/* request the cancellation of the running tasks */
			getLocalProbeTaskClient().requestCancel(runningTasksList);
			
			/* close() is not called if the command is invoked from preWindowShellClose() */
			if(event.getTrigger() != null) {
				activeWorkbenchWindow.getWorkbench().close();
			}
		}
		
		return confirm;
	}
	
	private boolean showConfirmDialog(List<Long> runningTasksList) {
		
		if(runningTasksList.size() > 0) {
			String msgTitle =  runningTasksList.size() + " tasks are running in the local probe";
			return MessageDialog.openConfirm(activeWorkbenchWindow.getShell(), msgTitle , 
				runningTasksList.size() + " tasks are running in the local probe if you quit netifera now the tasks will be stopped. Do you want to quit now?");
		} else {
			return true;
		}
	}
	
	private ITaskClient getLocalProbeTaskClient() {
		IProbe localProbe = ApplicationPlugin.getDefault().getProbeManager().getLocalProbe();
		ITaskClient taskClient = localProbe.getTaskClient();
		return taskClient;
	}
	
	private List<Long> getRunningTasksList() {
		List<Long> runningTasksList = new ArrayList<Long>();
		ITaskStatus[] currentTasks = getLocalProbeTaskClient().getCurrentTasks();

		for(ITaskStatus task : currentTasks) {
			if(task.isRunning()) {
				runningTasksList.add(task.getTaskId());
			}
		}
		
		return runningTasksList;
	}
}
