package com.netifera.platform.net.daemon.sniffing.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ConfigureAction extends Action {
	public final static String ID = "sniffer-configure-action";

	private final SniffingActionManager manager;

	ConfigureAction(SniffingActionManager manager) {
		setId(ID);
		setToolTipText("Configure Sniffing Service");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/configure.png"));
	
		this.manager = manager;
		
	}
	
	public void run() {
		ConfigPanel panel = new ConfigPanel(PlatformUI.getWorkbench().getDisplay().getActiveShell(), manager);
		panel.open();
	}
	

}
