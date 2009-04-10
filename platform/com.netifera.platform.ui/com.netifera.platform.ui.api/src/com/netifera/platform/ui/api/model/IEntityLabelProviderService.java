package com.netifera.platform.ui.api.model;

import org.eclipse.swt.graphics.Image;

import com.netifera.platform.api.model.IShadowEntity;

/**
 * This is an internal Netifera interface for the OSGi service which acts as a registry
 * for entity labeling information.
 * It is not intended to be implemented by plugin authors.
 *
 */
public interface IEntityLabelProviderService {
	String getText(IShadowEntity entity);
	String getFullText(IShadowEntity entity);
	Image getImage(IShadowEntity entity);
	String getInformation(IShadowEntity entity);
	int getSortingCategory(IShadowEntity entity);
	Integer compare(IShadowEntity e1, IShadowEntity e2);
}
