package com.netifera.platform.ui.probe;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.ui.actions.SpaceAction;
import com.netifera.platform.ui.spaces.SpaceEditorInput;

public class OpenSpaceAction extends SpaceAction {
	private final IProbe probe;
	private final ILogger logger;
	private final IWorkbenchPage page;

	public OpenSpaceAction(IProbe probe, ILogger logger) {
		super("Open New Space For This Probe");
		setImageDescriptor(Activator.getDefault().getImageCache().getDescriptor("icons/new_space.png"));
		this.probe = probe;
		this.logger = logger;
		this.page = getActivePage();
	}
	

	private IWorkbenchPage getActivePage() {
		/* hack around stupid java 'final' rule for anonymous inner classes */
		final IWorkbenchPage[] pageHolder = new IWorkbenchPage[1];
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			public void run() {
				final IWorkbench wb = PlatformUI.getWorkbench();
				if(wb == null) return;
				final IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
				if(window == null) return;
				pageHolder[0] = window.getActivePage();
			}
			
		});
		return pageHolder[0];
	}
	public void run() {
		if(page == null) {
			logger.warning("Cannot open editor on probe because no active page was found");
			return;
		}
		final IWorkspace workspace = Activator.getDefault().getModel().getCurrentWorkspace();
		final ISpace space = workspace.createSpace(probe.getEntity(), probe);
		space.open();
		
		final IEditorInput input = new SpaceEditorInput(space);
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			public void run() {
				try {
					page.openEditor(input, SpaceEditorInput.ID);
				} catch(PartInitException e) {
					logger.error("Failed to open editor", e);
				}
				
			}
			
		});
		
	}

}
