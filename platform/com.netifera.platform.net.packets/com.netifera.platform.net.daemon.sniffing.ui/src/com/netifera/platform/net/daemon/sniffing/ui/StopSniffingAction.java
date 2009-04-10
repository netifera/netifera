package com.netifera.platform.net.daemon.sniffing.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.net.daemon.sniffing.ISniffingDaemon;

public class StopSniffingAction extends Action {
	public final static String ID = "stop-sniffing-action";

	private final SniffingActionManager manager;
	
	public StopSniffingAction(SniffingActionManager manager) {
		setId(ID);
		this.manager = manager;
		setToolTipText("Stop Sniffing Service");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/stop_16x16.png"));
	}
	
	public void run() {
		final ISniffingDaemon daemon = Activator.getDefault().getSniffingDaemon();
		if(daemon == null) {
			manager.setFailed("No sniffing service found");
			return;
		}

		new Thread(new Runnable() {
			public void run() {
				daemon.stop();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						manager.setState();
					}
				});
			}
		}).start();
	}
}
