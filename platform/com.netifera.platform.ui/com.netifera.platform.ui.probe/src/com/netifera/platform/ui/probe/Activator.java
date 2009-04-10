package com.netifera.platform.ui.probe;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.ui.images.ImageCache;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.netifera.platform.ui.probe";

	private static Activator plugin;
	
	
	private ServiceTracker probeManagerTracker;
	private ServiceTracker modelTracker;

	private ImageCache imageCache;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		imageCache = new ImageCache(PLUGIN_ID);
		
		probeManagerTracker = new ServiceTracker(context, IProbeManagerService.class.getName(), null);
		probeManagerTracker.open();
		
		modelTracker = new ServiceTracker(context, IModelService.class.getName(), null);
		modelTracker.open();
	}

	
	@Override
	public void stop(BundleContext context) throws Exception {
		imageCache.dispose();
		imageCache = null;
		plugin = null;
		super.stop(context);
	}

	
	public static Activator getDefault() {
		return plugin;
	}
	
	public IProbeManagerService getProbeManager() {
		return (IProbeManagerService) probeManagerTracker.getService();
	}
	
	public IModelService getModel() {
		return (IModelService) modelTracker.getService();
	}
	
	public ImageCache getImageCache() {
		return imageCache;
	}
}
