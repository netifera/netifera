package com.netifera.platform.net.daemon.sniffing.ui;

import com.netifera.platform.api.system.ISystemService;
import com.netifera.platform.ui.util.Sudo;


public class BackdoorSetup {
	/* Mac OS X and Linux errno.h */
	static final int EPERM = 1; 
	
	private static final ISystemService systemService = Activator.getDefault().getSystemService();

	private BackdoorSetup() {
	}

	public static boolean isInstalled() {
		//XXX this harcoded 0 works for Mac and Linux but should be changed
		//for a new backdoor command to verify installation and privileges
		final int s = systemService.backdoor_request(0);
		if(s <= 0) { //XXX should be < 0, 0 is success?
			final int errno = systemService.getErrno();
			if(errno ==  EPERM) {
				return false;
			}
			return false;
		}
		return true;
	}
	
	public static boolean setInstall(boolean install) {
		if(install) {
			return install();
		}
		else {
			return uninstall();
		}
	}
	
	public static boolean install() {
		Sudo sudo = new Sudo();
		if(!sudo.canExecute()) {
			return false;
		}
		String backdoorPath =  systemService.backdoor_path();
		return sudo.system("chown root:root " + backdoorPath + ";chmod 4755 " + backdoorPath);
	}
	
	public static boolean uninstall() {
		Sudo sudo = new Sudo();
		if(!sudo.canExecute()) {
			return false;
		}
		String backdoorPath =  systemService.backdoor_path();
		return sudo.system("chmod 0755 " + backdoorPath);
	}
}
