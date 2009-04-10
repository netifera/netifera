package com.netifera.platform.ui.images;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;


public class ImageCache {

	private final Map<ImageDescriptor, Image> imageMap = new HashMap<ImageDescriptor, Image>();
	private final String pluginId;

	
	public ImageCache(String id) {
		pluginId = id;
	}

	public Image get(String key) {
		return get(getDescriptor(key));
	}

	public ImageDescriptor getDescriptor(String key) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, key);
	}

	public Image get(ImageDescriptor descriptor) {

		if (descriptor == null) {
			return null;
		}

		synchronized (imageMap) {
			/* if descriptor not contained in map create image and put it */
			if (!imageMap.containsKey(descriptor)) {
				imageMap.put(descriptor, descriptor.createImage());
			}

			return imageMap.get(descriptor);
		}
	}

	public Image getDecorated(String key, String overlayKeys[]) {
		Image baseImage = get(key);
		if (baseImage == null) {
			return null;
		}

		ImageDescriptor[] overlays = new ImageDescriptor[5];
		for (int i=0; i<5; i++)
			if (overlayKeys[i] != null) {
				overlays[i] = getDescriptor(overlayKeys[i]);
			}

		for (ImageDescriptor any: overlays)
			if (any != null) {
				ImageDescriptor descriptor = new DecorationOverlayIcon(baseImage, overlays);
				return get(descriptor);
			}
		
		return baseImage;
	}

	public synchronized void dispose() {
		for (Image image : imageMap.values()) {
			image.dispose();
		}
		imageMap.clear();
	}
}
