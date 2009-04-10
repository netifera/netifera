package com.netifera.platform.net.wifi.ui.toolbar;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.net.wifi.ui.Activator;

public class ConfigureWifiSnifferAction extends Action {
	public final static String ID = "sniffer-configure-action";
	private WifiToolbar toolbar;
	
	public ConfigureWifiSnifferAction(WifiToolbar toolbar) {
		setId(ID);
		this.toolbar = toolbar;
		setToolTipText("Configure Wireless Sniffer");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/configure.png"));
	}
	
	public void run() {
		final ConfigPanel panel = new ConfigPanel(PlatformUI.getWorkbench().getDisplay().getActiveShell(), toolbar);
		panel.open();
	}

	

}
