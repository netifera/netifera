package com.netifera.platform.net.internal.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.netifera.platform.ui.images.ImageCache;

public class Activator extends AbstractUIPlugin {
	public final static String PLUGIN_ID = "com.netifera.platform.net.ui";

	private ImageCache imageCache;
	
	private static Activator instance;
	public static Activator getInstance() {
		return instance;
	}

	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		imageCache = new ImageCache(PLUGIN_ID);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		imageCache.dispose();
		instance = null;
		super.stop(context);
	}
	
	public ImageCache getImageCache() {
		return imageCache;
	}
}
