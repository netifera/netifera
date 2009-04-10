package com.netifera.platform.ui.api.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;

import com.netifera.platform.api.model.IShadowEntity;

/**
 * This interface implements an OSGi service which extends the Entity Label Provider
 * Service.
 * 
 * @see com.netifera.platform.ui.api.model.IEntityLabelProviderService
 *
 */
public interface IEntityLabelProvider {
	
	/**
	 * Returns a <code>String</code> which describes the given entity when displayed in 
	 * GUI views.
	 * @param e The entity to provide a description for.
	 * @return String describing the entity, or null
	 */
	String getText(IShadowEntity e);

	/**
	 * @param e The entity to provide a description for.
	 * @return String describing the entity, or null
	 */
	String getFullText(IShadowEntity e);
	
	/**
	 * Returns an <code>Image</code> to use as icon for the given entity
	 * @param e The entity to provide an icon for.
	 * @return Image for that icon, or null
	 */
	Image getImage(IShadowEntity e);

    /**
     * Returns an image that is based on the given image,
     * but decorated with additional information relating to the state
     * of the provided entity.
     * 
     * @param image the input image to decorate, or <code>null</code> if the entity has no image
     * @param element the element whose image is being decorated
     * @return the decorated image, or <code>null</code> if no decoration is to be applied
	 */
	Image decorateImage(Image image, IShadowEntity e);


	/**
	 * Return a sorting category for the given entity, or null if not handled
	 */
	Integer getSortingCategory(IShadowEntity e);
	
	/**
	 * Compare the entities e1 and e2 to see where to put them in visual representations
	 * where order makes sense (for example tree).
	 * 
	 * @param e1 
	 * @param e2
	 * @return null if not handled
	 */
	Integer compare(IShadowEntity e1, IShadowEntity e2);
	
	/**
	 * 
	 */
	void dispose();
}
