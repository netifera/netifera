package com.netifera.platform.ui.workbench;

import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public class WorkbenchChangeManager {
	
	private final IWorkbenchWindow window;
	private final String perspectiveId;
	private final IWorkbenchChangeListener changeListener;
	public WorkbenchChangeManager(IWorkbenchWindow window, String perspectiveId, IWorkbenchChangeListener changeListener) {
		this.window = window;
		this.perspectiveId = perspectiveId;
		this.changeListener = changeListener;
	}
	
	public void initialize() {
	
		IWorkbenchPage page = window.getActivePage();
		if(page == null) {
			window.addPageListener(createPageListener());
		} else {
			activePageOpened(page);
		}
		
	}
	
	private IPageListener createPageListener() {
		return new IPageListener() {

			public void pageActivated(IWorkbenchPage page) {
				activePageOpened(page);
				window.removePageListener(this);				
			}
			public void pageClosed(IWorkbenchPage page) {}
			public void pageOpened(IWorkbenchPage page) {}
		};
	}
	
	private void activePageOpened(IWorkbenchPage page) {
	
		changeListener.activePageOpened(page);
		
		window.addPerspectiveListener(createPerspectiveListener());
		
		IPerspectiveDescriptor perspective = page.getPerspective();
		if(perspective != null && perspective.getId().equals(perspectiveId)) {
			changeListener.perspectiveOpened();
		} else {
			changeListener.perspectiveClosed();
		}
		
		page.addPartListener(createPartListener());
	}
	
	private IPerspectiveListener createPerspectiveListener() {
		return new IPerspectiveListener() {

			public void perspectiveActivated(IWorkbenchPage page,
					IPerspectiveDescriptor perspective) {
				if(perspective.getId().equals(perspectiveId)) {
					changeListener.perspectiveOpened();
				} else {
					changeListener.perspectiveClosed();
				}
				
			}
			public void perspectiveChanged(IWorkbenchPage page,
					IPerspectiveDescriptor perspective, String changeId) {}
			
		};
	}
	
	private IPartListener createPartListener() {
		return new IPartListener() {

			public void partActivated(IWorkbenchPart part) {
				changeListener.partChange();				
			}

			public void partClosed(IWorkbenchPart part) {
				changeListener.partChange();				
			}
			
			public void partBroughtToTop(IWorkbenchPart part) {}
			public void partDeactivated(IWorkbenchPart part) {}
			public void partOpened(IWorkbenchPart part) {
				changeListener.partChange();
			}
			
		};
	}
	
	

}
