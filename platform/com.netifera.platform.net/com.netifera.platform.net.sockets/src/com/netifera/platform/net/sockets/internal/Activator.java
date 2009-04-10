package com.netifera.platform.net.sockets.internal;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.netifera.platform.net.sockets.ISocketEngineService;


public class Activator implements BundleActivator {
	private ServiceTracker socketEngineTracker;

	private static Activator instance;
	
	public static Activator getInstance() {
		return instance;
	}
	
	public void start(BundleContext context) throws Exception {
		instance = this;
		
		socketEngineTracker = new ServiceTracker(context, ISocketEngineService.class.getName(), null);
		socketEngineTracker.open();
	}
	
	public void stop(BundleContext arg0) throws Exception {
	}
	
	public ISocketEngineService getSocketEngine() {
		return (ISocketEngineService) socketEngineTracker.getService();
	}
}
