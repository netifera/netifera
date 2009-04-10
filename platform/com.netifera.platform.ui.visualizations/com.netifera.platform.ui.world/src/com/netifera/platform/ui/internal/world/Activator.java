package com.netifera.platform.ui.internal.world;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.net.geoip.IGeoIPService;
import com.netifera.platform.ui.api.model.IEntityLabelProviderService;
import com.netifera.platform.ui.images.ImageCache;


public class Activator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "com.netifera.platform.ui.world";

	// The shared instance
	private static Activator plugin;

	public static Activator getDefault() {
		return plugin;
	}

	private ImageCache imageCache;

	private ServiceTracker modelTracker;
	private ServiceTracker modelLabelsTracker;
	private ServiceTracker geoipServiceTracker;
	

	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		imageCache = new ImageCache(PLUGIN_ID);
		
		modelTracker = new ServiceTracker(context, IModelService.class.getName(), null);
		modelTracker.open();
		
		modelLabelsTracker = new ServiceTracker(context, IEntityLabelProviderService.class.getName(), null);
		modelLabelsTracker.open();
		
		geoipServiceTracker = new ServiceTracker(context, IGeoIPService.class.getName(), null);
		geoipServiceTracker.open();
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
	
	public IEntityLabelProviderService getLabelProvider() {
		return (IEntityLabelProviderService) modelLabelsTracker.getService();
	}
	
	public IGeoIPService getGeoIPService() {
		return (IGeoIPService) geoipServiceTracker.getService();
	}
	
	public ImageCache getImageCache() {
		return imageCache;
	}
}
