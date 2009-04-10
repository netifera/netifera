package com.netifera.platform.host.filesystem.ui.probe;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.host.filesystem.IFileSystem;
import com.netifera.platform.host.filesystem.IFileSystemFactory;
import com.netifera.platform.host.filesystem.ui.FileSystemView;
import com.netifera.platform.host.internal.filesystem.ui.Activator;
import com.netifera.platform.ui.actions.SpaceAction;

public class OpenProbeFileSystemViewAction extends SpaceAction {
	private final IWorkbenchPage page;
	private final ILogger logger;
	private final IProbe probe;
	private final IFileSystemFactory fileSystemFactory;
	
	public OpenProbeFileSystemViewAction(ILogger logger, IProbe probe, IFileSystemFactory factory) {
		super("Browse File System");
		setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/folders.png"));
		page = getActivePage();
		this.logger = logger;
		this.probe = probe;
		this.fileSystemFactory = factory;
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
			logger.warning("Error opening filesystem view", e);
		}
	}
	private void openProcessView() throws PartInitException {
		IViewPart view = page.showView(FileSystemView.ID, "File System", IWorkbenchPage.VIEW_ACTIVATE);
		if(!(view instanceof FileSystemView))
			return;
		FileSystemView fsView = (FileSystemView) view;
		IFileSystem filesystem = fileSystemFactory.createForProbe(probe);
		fsView.setInput(filesystem);
		fsView.setName(probe.getName());
	}
	
}
