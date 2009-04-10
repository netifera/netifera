package com.netifera.platform.ui.spaces.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class NewSpace implements IWorkbenchWindowActionDelegate {

	private SpaceCreator creator;
	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		creator = new SpaceCreator(window);

	}

	public void run(IAction action) {
		creator.create();
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

}
