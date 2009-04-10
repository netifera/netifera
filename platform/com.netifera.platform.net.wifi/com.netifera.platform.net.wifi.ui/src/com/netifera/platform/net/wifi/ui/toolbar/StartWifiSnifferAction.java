package com.netifera.platform.net.wifi.ui.toolbar;

import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.wifi.pcap.IWirelessCaptureInterface;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffingDaemon;
import com.netifera.platform.net.wifi.ui.Activator;

public class StartWifiSnifferAction extends Action {
	public final static String ID = "start-wifi-action";
	private final WifiToolbar toolbar;

	StartWifiSnifferAction(WifiToolbar toolbar) {
		setId(ID);
		this.toolbar = toolbar;
		setToolTipText("Start Wireless Sniffer");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/start_16x16.png"));
	}

	public void run() {
		final ISpace space = Activator.getDefault().getCurrentSpace();
		if(space == null)
			return;
		
		final IWifiSniffingDaemon daemon = Activator.getDefault().getWifiDaemon();
		if(daemon == null) {
			toolbar.setFailed("No wireless service found");
			return;
		}
		if(!hasInterfacesAvailable(daemon)) {
			toolbar.asynchSetState();
			return;
		}
		
		new Thread(new Runnable() {

			public void run() {
				daemon.start(space.getId());
				toolbar.asynchSetState();
			}
			
		}).start();

	}

	private boolean hasInterfacesAvailable(IWifiSniffingDaemon daemon) {
		final Collection<IWirelessCaptureInterface> interfaces = daemon.getWirelessInterfaces();
		if(interfaces.isEmpty()) 
			return false;
		return hasEnabledInterfaces(interfaces);
	}

	private boolean hasEnabledInterfaces(
			Collection<IWirelessCaptureInterface> interfaces) {
		for(ICaptureInterface iface : interfaces) {
			if(iface.captureAvailable())
				return true;
		}
		return false;
	}
	
}
