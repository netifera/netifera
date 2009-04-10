package com.netifera.platform.ui.internal.graphs;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.ui.images.ImageCache;

public class Activator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "com.netifera.platform.ui.graphs";

	// The shared instance
	private static Activator plugin;

	public static Activator getDefault() {
		return plugin;
	}
	
	private ImageCache imageCache;
	
	private ServiceTracker modelTracker;

	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		imageCache = new ImageCache(PLUGIN_ID);
		
		modelTracker = new ServiceTracker(context, IModelService.class.getName(), null);
		modelTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		imageCache.dispose();
		imageCache = null;
		
		plugin = null;
		super.stop(context);
	}

	public IModelService getModel() {
		return (IModelService) modelTracker.getService();
	}
	
	public ImageCache getImageCache() {
		return imageCache;
	}
}
