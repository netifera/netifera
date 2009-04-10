package com.netifera.platform.host.filesystem.ui.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;

import com.netifera.platform.host.internal.filesystem.ui.Activator;

public class RenameAction extends AbstractFileSystemAction {

	public RenameAction(ISelectionProvider selectionProvider) {
		super(selectionProvider);
		setText("&Rename");
		setToolTipText("Rename");
		ImageDescriptor icon =  Activator.getInstance().getImageCache().getDescriptor("icons/rename.png");
		setImageDescriptor(icon);
	}
	
	@Override
	public void run() {
        throw new UnsupportedOperationException("Not implemented"); 	}
}
