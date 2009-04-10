package com.netifera.platform.ui.model.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.ui.UIPlugin;
import com.netifera.platform.ui.api.model.IEntityInformationProvider;
import com.netifera.platform.ui.api.model.IEntityLabelProvider;
import com.netifera.platform.ui.api.model.IEntityLabelProviderService;
import com.netifera.platform.ui.images.ImageCache;

public class EntityLabelProviderService implements IEntityLabelProviderService {
	
	private final static String UNKNOWN_IMAGE = "icons/unknown_entity.png";
	private final ImageCache images = new ImageCache(UIPlugin.PLUGIN_ID);
	
	private final List<IEntityLabelProvider> labelProviders = new ArrayList<IEntityLabelProvider>();
	private final List<IEntityInformationProvider> informationProviders = new ArrayList<IEntityInformationProvider>();

	public synchronized String getText(IShadowEntity entity) {
		for(IEntityLabelProvider provider : labelProviders) {
			String text = provider.getText(entity);
			if(text != null)
				return text;
		}
		// not found
		return (entity.getTypeName().matches("[AEIOU].*") ? "an " : "a ") + entity.getTypeName();
	}

	public synchronized String getFullText(IShadowEntity entity) {
		for(IEntityLabelProvider provider : labelProviders) {
			String text = provider.getFullText(entity);
			if(text != null)
				return text;
		}
		// not found
		return getText(entity);
	}

	public synchronized Image getImage(IShadowEntity entity) {
		for(IEntityLabelProvider provider : labelProviders) {
			Image image = provider.getImage(entity);
			if(image != null)
				return getDecorated(image, entity);
		}
		
		// not found
		return images.get(UNKNOWN_IMAGE);
	}

	private Image getDecorated(Image baseImage, IShadowEntity entity) {
		for(IEntityLabelProvider provider : labelProviders) {
			Image image = provider.decorateImage(baseImage, entity);
			if (image != null) baseImage = image;
		}

		return baseImage;
	}
	
	public synchronized String getInformation(IShadowEntity entity) {
		StringBuffer buffer = new StringBuffer();
		for(IEntityInformationProvider provider : informationProviders) {
			String information = provider.getInformation(entity);
			if(information != null)
				buffer.append(information);
		}
		return buffer.toString();
	}
	
	public synchronized int getSortingCategory(IShadowEntity entity) {
		for(IEntityLabelProvider provider : labelProviders) {
			Integer category = provider.getSortingCategory(entity);
			if (category != null)
				return category;
		}
		return 10;
	}

	public synchronized Integer compare(IShadowEntity e1, IShadowEntity e2) {
		for(IEntityLabelProvider provider : labelProviders) {
			Integer value = provider.compare(e1, e2);
			if (value != null)
				return value;
		}
		return null;
	}
	
	protected synchronized void registerLabelProvider(IEntityLabelProvider provider) {
		labelProviders.add(provider);
	}
	
	protected synchronized void unregisterLabelProvider(IEntityLabelProvider provider) {
		labelProviders.remove(provider);
	}

	protected synchronized void registerInformationProvider(IEntityInformationProvider provider) {
		informationProviders.add(provider);
	}
	
	protected synchronized void unregisterInformationProvider(IEntityInformationProvider provider) {
		informationProviders.remove(provider);
	}
}
