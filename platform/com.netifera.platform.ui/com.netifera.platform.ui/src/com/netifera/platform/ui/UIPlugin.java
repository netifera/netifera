package com.netifera.platform.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.ui.images.ImageCache;

public class UIPlugin extends AbstractUIPlugin {
	public final static String PLUGIN_ID = "com.netifera.platform.ui";

	private ServiceTracker probeManagerTracker;
	private ImageCache imageCache;

	private static UIPlugin plugin;
	public static UIPlugin getPlugin() {
		return plugin;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		probeManagerTracker = new ServiceTracker(context, IProbeManagerService.class.getName(), null);
		probeManagerTracker.open();
		
		imageCache = new ImageCache(PLUGIN_ID);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		imageCache.dispose();
		imageCache = null;
	}
	
	public IProbeManagerService getProbeManager() {
		return (IProbeManagerService) probeManagerTracker.getService();
	}
	
	public ImageCache getImageCache() {
		return imageCache;
	}
}
