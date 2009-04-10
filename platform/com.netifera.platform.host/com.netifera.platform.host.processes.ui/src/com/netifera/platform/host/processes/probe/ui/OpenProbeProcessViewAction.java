package com.netifera.platform.host.processes.probe.ui;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.host.internal.processes.ui.Activator;
import com.netifera.platform.host.processes.IProcessManager;
import com.netifera.platform.host.processes.IProcessManagerFactory;
import com.netifera.platform.host.processes.ui.ProcessListView;
import com.netifera.platform.ui.actions.SpaceAction;


public class OpenProbeProcessViewAction extends SpaceAction {
	private final IWorkbenchPage page;
	private final ILogger logger;
	private final IProbe probe;
	private final IProcessManagerFactory factory;
	
	public OpenProbeProcessViewAction(ILogger logger, IProbe probe, IProcessManagerFactory factory) {
		super("Open Process List");
		setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/processes.png"));
		page = getActivePage();
		this.logger = logger;
		this.probe = probe;
		this.factory = factory;
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
		if(page == null) 
			return;
		try {
			openProcessView();
		} catch (PartInitException e) {
			logger.warning("Error opening process view", e);
		}
	}
	
	private void openProcessView() throws PartInitException {
		IViewPart view = page.showView(ProcessListView.ID, "Process List", IWorkbenchPage.VIEW_ACTIVATE);
		if(view instanceof ProcessListView) {
			setProcessInput((ProcessListView) view);
		}
	}
	
	private void setProcessInput(ProcessListView view) {
		
		IProcessManager processManager = factory.createForProbe(probe);
		view.setInput(processManager);
		view.setName(probe.getName());
		
	}
	
}
