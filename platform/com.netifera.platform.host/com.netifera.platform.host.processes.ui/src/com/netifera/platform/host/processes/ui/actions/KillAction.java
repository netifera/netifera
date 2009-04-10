package com.netifera.platform.host.processes.ui.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;

import com.netifera.platform.host.internal.processes.ui.Activator;
import com.netifera.platform.host.processes.Process;

public class KillAction extends AbstractProcessAction {

	public KillAction(ISelectionProvider selectionProvider) {
		super(selectionProvider);
		setText("&Kill");
		setToolTipText("Kill Process");
		ImageDescriptor icon =  Activator.getInstance().getImageCache().getDescriptor("icons/stop.png");
		setImageDescriptor(icon);
	}
	
	@Override
	public void run() {
		for (Object o: getSelection().toArray()) {
			if (o instanceof Process)
				if (MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Are you sure?", "Kill "+o+"?")) {
					final Process process = (Process)o;
					new Thread(new Runnable() {
						public void run() {
							try {
								if (!process.kill())
									Activator.getInstance().getBalloonManager().warning("Cannot kill "+process);
							} catch (Exception e) {
								Activator.getInstance().getBalloonManager().error(e.toString());
								e.printStackTrace();
							}
						}
					}).start();
				}
		}
	}
}