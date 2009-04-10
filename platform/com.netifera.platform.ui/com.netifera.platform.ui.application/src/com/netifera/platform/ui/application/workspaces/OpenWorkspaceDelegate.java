package com.netifera.platform.ui.application.workspaces;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


public class OpenWorkspaceDelegate implements IWorkbenchWindowActionDelegate {

	public void dispose() {

	}

	public void init(IWorkbenchWindow window) {

	}

	public void run(IAction action) {
		OpenWorkspaceHandler.openChoseWorkspaceDialog(true);
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
}
