package com.netifera.platform.host.filesystem.ui.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;

import com.netifera.platform.host.filesystem.File;
import com.netifera.platform.host.internal.filesystem.ui.Activator;

public class DeleteAction extends AbstractFileSystemAction {

	public DeleteAction(ISelectionProvider selectionProvider) {
		super(selectionProvider);
		setText("&Delete");
		setToolTipText("Delete File");
		ImageDescriptor icon =  Activator.getInstance().getImageCache().getDescriptor("icons/delete.png");
		setImageDescriptor(icon);
	}
	
	@Override
	public void run() {
		for (Object o: getSelection().toArray()) {
			if (o instanceof File)
				if (MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Are you sure?", "Delete "+o+"?")) {
					final File file = (File)o;
					new Thread(new Runnable() {
						public void run() {
							try {
								if (!file.delete())
									Activator.getInstance().getBalloonManager().warning("Cannot delete "+file);
							} catch (Exception e) {
								Activator.getInstance().getBalloonManager().error(e.toString());
							}
						}
					}).start();
				}
		}
	}
}