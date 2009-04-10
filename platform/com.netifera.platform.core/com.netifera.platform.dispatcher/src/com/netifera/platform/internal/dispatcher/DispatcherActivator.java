package com.netifera.platform.internal.dispatcher;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class DispatcherActivator implements BundleActivator {

	private BundleContext context;
	private static DispatcherActivator instance;
	public static DispatcherActivator getInstance() {
		return instance;
	}
	public void start(BundleContext context) throws Exception {
		instance = this;		
		this.context = context;
	}

	public void stop(BundleContext context) throws Exception {		
	}
	
	public String getProperty(String name) {
		return context.getProperty(name);
	}

}
