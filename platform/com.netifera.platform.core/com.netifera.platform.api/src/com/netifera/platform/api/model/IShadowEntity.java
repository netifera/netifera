package com.netifera.platform.api.model;

/**
 * IShadowEntity instances are copies of model entities which are stored in 
 * user interface structures such as trees, graphs, and lists.  Every shadow
 * entity instance is associated with exactly one instance of a structure
 * context object.
 * 
 * @see com.netifera.platform.api.model.IStructureContext
 *
 */
public interface IShadowEntity extends IEntity {
	
	/**
	 * Return the structure context that this shadow entity belongs to. 
	 * 
	 * @see com.netifera.platform.api.model.IStructureContext
	 * @return The structure context instance associated with this shadow entity.
	 */
	IStructureContext getStructureContext();
	
	/**
	 * A string description of this entity type.  Each implementation of 
	 * IEntity is identified by a unique string.  For example, the type string
	 * of ProbeEntity is "probe".
	 * @return The string description of this entity type.
	 */
	String getTypeName();
	
	/**
	 * Access the entity this IShadowEntity was cloned from.  
	 * @return The real entity associated with this shadow entity.
	 */
	IEntity getRealEntity();
	
	/**
	 * Search the structure context associated with this entity for the specified
	 * entity.  If found, the shadow entity copy from the same structure context will
	 * be returned.
	 * 
	 * @return The shadow entity copy of the given entity from the structure context
	 * associated with this shadow entity.
	 * 
	 * @see com.netifera.platform.api.model.IStructureContext
	 */
	IShadowEntity searchEntity(IEntity entity);
	
	/**
	 * Dispose this shadow entity, remove it from the synchronization list
	 * in the original entity.
	 */
	void dispose();
	
	long getViewId();
}
