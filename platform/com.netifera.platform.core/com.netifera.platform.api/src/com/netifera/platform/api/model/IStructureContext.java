package com.netifera.platform.api.model;

/**
 * A structure context holds structure information that relates a set
 * of shadow entities that are being displayed in the user interface.
 * 
 * Every shadow entity must belong to exactly one structure context which can
 * be accessed through the {@link IShadowEntity#getStructureContext()} method.
 * 
 * This interface is meant to be extended to provide interfaces for
 * specific types of structure contexts for data structures such as
 * trees, lists, and graphs.
 *
 */
public interface IStructureContext {
	
	/**
	 * Search for the specified entity in the set of shadow entities associated
	 * with this structure context.
	 * @param entity The entity to search for.
	 * @return The shadow entity in this structure context for the 
	 * 	given entity or <code>null</code> if the entity is not found.
	 */
	IShadowEntity searchEntity(IEntity entity);
	
	/**
	 * Search for the specified entity (by entity id) in the set of shadow entities
	 * associated with this structure context.
	 * @param entity The id of the entity to search for.
	 * @return The shadow entity in this structure context for the 
	 * 	given entity or <code>null</code> if the entity is not found.
	 */
	IShadowEntity searchEntityById(long entityId);

}
