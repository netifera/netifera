package com.netifera.platform.ui.workbench;

import org.eclipse.ui.IWorkbenchPage;

public interface IWorkbenchChangeListener {
	void perspectiveOpened();
	void perspectiveClosed();
	void partChange();
	void activePageOpened(IWorkbenchPage page);

}
