package com.netifera.platform.host.internal.terminal.ui.pty;

import org.eclipse.tm.internal.terminal.connector.TerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.host.internal.terminal.ui.Activator;
import com.netifera.platform.host.terminal.ui.view.TerminalView;
import com.netifera.platform.ui.actions.SpaceAction;

public class OpenProbePtyTerminalAction extends SpaceAction {

	private final IWorkbenchPage page;
	private final ILogger logger;
	private final IProbe probe;
	
	public OpenProbePtyTerminalAction(IProbe probe, ILogger logger) {
		super("Open Shell");
		setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/terminal_view.png"));
		this.page = getActivePage();
		this.logger = logger;
		this.probe = probe;
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
			logger.warning("Error opening terminal view", e);
		}
	}
	
	private void openProcessView() throws PartInitException {
		IViewPart view = page.showView("org.eclipse.tm.terminal.view.TerminalView", 
				"SecondaryTerminal" + System.currentTimeMillis(), IWorkbenchPage.VIEW_ACTIVATE);
		TerminalView terminal = (TerminalView) view;
		
		TerminalConnector.Factory factory = new TerminalConnector.Factory() {

			public TerminalConnectorImpl makeConnector() throws Exception {
				return new PTYConnector(probe);
				
			}
			
		};
		terminal.setConnector(new TerminalConnector(factory, "pty-terminal", "Terminal"));
		terminal.setSpace(getSpace());
		terminal.onTerminalConnect();
		
		
	}
}
