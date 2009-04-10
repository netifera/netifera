package com.netifera.platform.api.model;

import java.util.Set;

import com.netifera.platform.api.iterables.IndexedIterable;

/**
 * This interface should not be implemented directly.  Subclass
 * {@link AbstractEntity} to create new model entity types.
 * 
 * @author brl
 *
 */
public interface IEntity {

	/**
	 * Get the workspace this entity belongs to.
	 * @return The workspace thise entity belongs to.
	 */
	IWorkspace getWorkspace();

	/**
	 * Return the unique id value for this entity instance.
	 * @return The unique id value for this entity instance.
	 */
	long getId();
	
	/**
	 * A string description of this entity type.  Each implementation of 
	 * IEntity is identified by a unique string.  For example, the type string
	 * of ProbeEntity is "probe".
	 * @return The string description of this entity type.
	 */
	String getTypeName();
	
	/**
	 * Create a copy of this entity which is suitable for temporary use in user
	 * interface structures such as trees, lists, and graphs.
	 * @param structure
	 * @return
	 */
	IShadowEntity shadowClone(IStructureContext structure);
	
	/**
	 * Create and return a {@link IEntityReference} reference to this entity.
	 * @return The newly created reference.
	 */
	IEntityReference createReference();
	
	/**
	 * Return true if this entity type is a 'realm creating' entity.
	 * @return true if this entity type is a 'realm creating' entity.
	 */
	boolean isRealmEntity();
	
	/**
	 * Return the entity id of the <i>realm</i> entity for this entity.
	 * 
	 * All entities belong to exactly one <i>realm</i> entity.
	 * 
	 * @return The entity id of the <i>realm</i> entity for this entity.
	 */
	long getRealmId();

	/**
	 * Attempt to adapt this entity to the specified class or interface.
	 * 
	 * @param adapterType The target class or interface to which we want to adapt
	 * @return The adapted entity (an instance of adapterType), or null if the adaptation is not possible.
	 */
	Object getAdapter(Class<?> adapterType);
	
	/**
	 * Attempt to adapt this entity to an iterable over the specified class or interface.
	 * 
	 * @param iterableType The target class or interface for the elements of the iterable to which we want to adapt
	 * @return The adapted entity (an instance of IndexedIterable<iterableType>), or null if the adaptation is not possible.
	 */
	IndexedIterable<?> getIterableAdapter(Class<?> iterableType);

	Set<String> getTags();
}
