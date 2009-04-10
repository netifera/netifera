package com.netifera.platform.host.filesystem.ui;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.UIPlugin;

import com.netifera.platform.host.filesystem.IFileSystem;
import com.netifera.platform.host.internal.filesystem.ui.Activator;
import com.netifera.platform.ui.actions.SpaceAction;

public abstract class OpenFileSystemViewAction extends SpaceAction {
	
	public OpenFileSystemViewAction(final String name) {
		super(name);
		setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/folders.png"));
	}

	public abstract IFileSystem createFileSystem();
	
	@Override
	public void run() {
		IFileSystem fileSystem = createFileSystem();
		
		try {
			IViewPart view = UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
					FileSystemView.ID,
					"SecondaryFileSystem" + System.currentTimeMillis(),
					IWorkbenchPage.VIEW_ACTIVATE);
			((FileSystemView)view).setInput(fileSystem);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
