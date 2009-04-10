package com.netifera.platform.internal.probe;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	private final String[] startBundles = { 
			"org.eclipse.equinox.ds",
			"org.eclipse.equinox.util"
	};
	
	private BundleContext context;
	private static Activator instance;
	
	public static Activator getInstance() {
		return instance;
	}

	public void start(BundleContext context) throws Exception {
		instance = this;
		this.context = context;
		
		Bundle[] bundles = context.getBundles();
		for(Bundle b : bundles) {
			if(isStartBundle(b) && ((b.getState() == Bundle.RESOLVED) || (b.getState() == Bundle.STARTING))) {
				b.start();
			}	
		}
		
	}

	public void stop(BundleContext context) throws Exception {
		
	}
	
	public String getProperty(String key) {
		return context.getProperty(key);
	}
	
	private boolean isStartBundle(Bundle bundle) {
		for(String bundleName : startBundles) {
			if(bundle.getSymbolicName().equals(bundleName))
				return true;
		}
		if(bundle.getHeaders().get("Service-Component") != null) {
			return true;
		}
		return false;
	}

}
