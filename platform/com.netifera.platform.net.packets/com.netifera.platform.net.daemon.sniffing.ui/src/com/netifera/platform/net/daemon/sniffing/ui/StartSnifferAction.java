package com.netifera.platform.net.daemon.sniffing.ui;

import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.net.daemon.sniffing.ISniffingDaemon;
import com.netifera.platform.net.pcap.ICaptureInterface;

/**
 * An action for starting sniffing daemon.
 * 
 * @see com.netifera.platform.net.daemon.sniffing.ISniffingDaemon
 * 
 *
 */
public class StartSnifferAction extends Action {
	public final static String ID = "start-sniffing-action";

	private final SniffingActionManager manager;

	StartSnifferAction(SniffingActionManager manager) {
		setId(ID);
		this.manager = manager;
		setToolTipText("Start Sniffing Service");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/start_16x16.png"));
	}

	public void run() {
		final ISpace space = Activator.getDefault().getCurrentSpace();
		if(space == null) {
			return;
		}
		final ISniffingDaemon daemon = Activator.getDefault().getSniffingDaemon();
		if(daemon == null) {
			manager.setFailed("No sniffing service found");
			return;
		}
		
		if(!hasInterfacesAvailable(daemon)) {
			manager.setState();
			return;
		}
		
		new Thread(new Runnable() {
			public void run() {
				daemon.start(space.getId());
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						manager.setState();
					}
				});
			}
		}).start();
	}
	
	
	private boolean hasInterfacesAvailable(ISniffingDaemon daemon) {
		final Collection<ICaptureInterface> interfaces = daemon.getInterfaces();
		if(interfaces.isEmpty()) {
			return false;
		}
		return hasEnabledInterface(interfaces);
	}
	private boolean hasEnabledInterface(Collection<ICaptureInterface> interfaces) {
		for(ICaptureInterface iface : interfaces) {
			if(iface.captureAvailable())
				return true;
		}
		return false;
	}
}
