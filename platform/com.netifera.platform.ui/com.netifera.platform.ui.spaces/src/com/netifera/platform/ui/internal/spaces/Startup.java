package com.netifera.platform.ui.internal.spaces;

import org.eclipse.ui.IStartup;


public class Startup implements IStartup {

	public void earlyStartup() {
		Activator.getDefault().initialize();
	}

}
