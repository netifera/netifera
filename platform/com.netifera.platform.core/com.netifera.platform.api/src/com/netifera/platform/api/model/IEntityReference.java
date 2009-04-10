package com.netifera.platform.api.model;

/**
 * Classes implementing this interface must also implement {@link
 * java.io.Serializable}.
 * 
 * @see com.netifera.platform.internal.model.EntityReference
 */
public interface IEntityReference {
	long getId();
	IEntityReference createClone();
	IEntity getEntity(IWorkspace workspace);
	void freeCachedEntity();
}
