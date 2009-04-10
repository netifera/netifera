package com.netifera.platform.ui.internal.tasks;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.ui.images.ImageCache;

public class TasksPlugin extends AbstractUIPlugin {
	public final static String PLUGIN_ID = "com.netifera.platform.ui.views.tasks";
	
	private static TasksPlugin plugin;
	public static TasksPlugin getPlugin() {
		return plugin;
	}
	
	private ImageCache imageCache;
	private ServiceTracker probeManagerTracker;

	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		imageCache = new ImageCache(PLUGIN_ID);
		probeManagerTracker = new ServiceTracker(context, IProbeManagerService.class.getName(), null);
		probeManagerTracker.open();
	}

	@Override
    public void stop(BundleContext context) throws Exception {
		plugin = null;
		imageCache.dispose();
		imageCache = null;
		super.stop(context);
	}
	
	public IProbeManagerService getProbeManager() {
		return (IProbeManagerService) probeManagerTracker.getService();
	}
	
	public ImageCache getImageCache() {
		return imageCache;
	}
}
