package com.netifera.platform.net.wifi.ui;

import org.eclipse.ui.IStartup;


public class Startup implements IStartup {

	public void earlyStartup() {
		Activator.getDefault().initialize();
	}

}
