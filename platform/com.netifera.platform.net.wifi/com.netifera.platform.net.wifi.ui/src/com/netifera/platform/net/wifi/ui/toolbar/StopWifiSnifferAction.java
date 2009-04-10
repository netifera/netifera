package com.netifera.platform.net.wifi.ui.toolbar;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.net.daemon.sniffing.ISniffingDaemon;
import com.netifera.platform.net.wifi.ui.Activator;

public class StopWifiSnifferAction extends Action {
	
	public final static String ID = "stop-wifi-action";
	private final WifiToolbar toolbar;

	StopWifiSnifferAction(WifiToolbar toolbar) {
		setId(ID);
		this.toolbar = toolbar;
		setToolTipText("Start Wireless Sniffer");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/stop_16x16.png"));
	}

	public void run() {
		final ISniffingDaemon daemon = Activator.getDefault().getWifiDaemon();
		if(daemon == null) {
			toolbar.setFailed("No sniffing service found");
			return;
		}
		
		new Thread(new Runnable() {

			public void run() {
				daemon.stop();
				toolbar.asynchSetState();				
			}
			
		}).start();
	}

}
