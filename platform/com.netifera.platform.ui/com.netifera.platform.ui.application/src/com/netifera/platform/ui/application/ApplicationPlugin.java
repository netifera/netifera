package com.netifera.platform.ui.application;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.util.tracker.ServiceTracker;

import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.api.probe.IProbeManagerService;

/**
 * The main plug-in class to be used in the desktop client.
 */
public class ApplicationPlugin extends AbstractUIPlugin {
	private final static String probeBundle = "com.netifera.platform.probe";
	private final static String modelBundle = "com.netifera.platform.model";
	//The shared instance.
	private static ApplicationPlugin plugin;
	private ServiceTracker modelTracker;
	private ServiceTracker probeManagerTracker;
	private ICoolBarManager coolbar;

	

	/**
	 * The constructor.
	 */
	public ApplicationPlugin() {
		plugin = this;
	}

	public void setCoolBar(ICoolBarManager coolbar) {
		this.coolbar = coolbar;
		
	}
	
	public ICoolBarManager getCoolBar() {
		return coolbar;
	}
	
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		modelTracker = new ServiceTracker(context, IModelService.class.getName(), null);
		modelTracker.open();
		
		probeManagerTracker = new ServiceTracker(context, IProbeManagerService.class.getName(), null);
		probeManagerTracker.open();
		
		startProbeBundle(context);
	
	}

	private void startProbeBundle(BundleContext context) throws BundleException {
		for(Bundle b : context.getBundles()) {
			if(b.getSymbolicName().equals(probeBundle) && (b.getState() == Bundle.RESOLVED))
				b.start();
			if(b.getSymbolicName().equals(modelBundle) && (b.getState() == Bundle.RESOLVED))
				b.start();
		}
	}
	
	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ApplicationPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("com.netifera.platform.ui.application", path);
	}
	
	public IModelService getModel() {
		try {
			return (IModelService) modelTracker.waitForService(5000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}
	
	public IProbeManagerService getProbeManager() {
		return (IProbeManagerService) probeManagerTracker.getService();
	}
}
