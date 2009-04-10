package com.netifera.platform.net.http.internal.service;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.netifera.platform.api.log.ILogManager;

public class Activator implements BundleActivator {
	private static Activator instance;
	private ServiceTracker logManagerTracker;
	
	public static Activator getInstance() {
		return instance;
	}
	
	public void start(BundleContext context) throws Exception {
		instance = this;
		
		logManagerTracker = new ServiceTracker(context, ILogManager.class.getName(), null);
		logManagerTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public ILogManager getLogManager() {
		return (ILogManager) logManagerTracker.getService();
	}
}
