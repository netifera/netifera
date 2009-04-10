package com.netifera.platform.demo.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.demo.IProbeDeployer;

public class Activator implements BundleActivator {

	private ServiceTracker probeDeployerTracker;
	private ServiceTracker probeManagerTracker;

	private static Activator plugin;

	public static Activator getDefault() {
		return plugin;
	}
	
	
	public void start(BundleContext context) throws Exception {
		plugin = this;
		probeDeployerTracker = new ServiceTracker(context, IProbeDeployer.class.getName(), null);
		probeDeployerTracker.open();
		
		probeManagerTracker = new ServiceTracker(context, IProbeManagerService.class.getName(), null);
		probeManagerTracker.open();
		
		
	}

	
	public void stop(BundleContext context) throws Exception {
	}
	
	public IProbeDeployer getProbeDeployer() {
		return (IProbeDeployer) probeDeployerTracker.getService();
	}
	
	public IProbeManagerService getProbeManager() {
		return (IProbeManagerService) probeManagerTracker.getService();
	}
	
	

}
