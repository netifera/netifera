package com.netifera.platform.api.model;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.model.layers.ILayerProvider;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tasks.ITaskRecord;

public interface IWorkspace {
	
	boolean open();
	void close();
	
	/**
	 * Save entity to database.
	 * @param <T> subclass of IEntity
	 * @param entity
	 */
	<T extends IEntity> void storeEntity(T entity);
	
	/**
	 * Add entity to the given space.
	 * @param entity
	 * @param spaceId the space where the entity should be added
	 */
	void addEntityToSpace(IEntity entity, long spaceId);

	/**
	 * Update a previously saved entity in the database and fire an 'update' model change event.
	 * 
	 *  @param <T> subclass of IEntity
	 *  @param entity The entity instance to update in the database
	 */
	<T extends IEntity> void updateEntity(T entity);
	
	/**
	 * Find all entities of a specified class.
	 * @param <T> subclass of IEntity
	 * @param klass
	 * @return a list of all entities of a specified IEntity subclass
	 */
	<T extends IEntity> List<T> findAll(Class<T> klass);
	
	/**
	 * Find entity by id.
	 * @param id
	 * @return the IEntity instance with the specified id or null if it is not found.
	 */
	IEntity findById(long id);

	/**
	 * Find entity by realm.
	 * @param realm
	 * @return a list of all entities in the specified realm
	 */
	<T extends IEntity> List<T> findByRealm(long realm);

	/**
	 * Find all entities of a specified class that match a provided predicate.
	 * @param <T> subclass of IEntity
	 * @param predicate instance with the match method to be evaluated
	 * @return a list of all entities a the specified class that match the provided predicate.
	 */
	<T extends IEntity> List<T> findByPredicate(IModelPredicate<T> predicate); 

	/**
	 * Find all entities of a specified class that match a provided predicate.
	 * @param <T> subclass of IEntity
	 * @param predicate instance with the match method to be evaluated
	 * @param comparator Comparator to sort the result
	 * @return a list of all entities a the specified class that match the provided predicate, sorted by the provided comparator.
	 */
	<T extends IEntity> List<T> findByPredicate(IModelPredicate<T> predicate, Comparator<T> comparator);
	
	/**
	 * Find all entities of a specified class that match a provided predicate.
	 * @param <T> subclass of IEntity
	 * @param predicate instance with the match method to be evaluated
	 * @param klass Class<T>  
	 * @return a list of all entities a the specified class that match the provided predicate.
	 */
	<T extends IEntity> List<T> findByPredicate(Class<T> klass, IModelPredicate<T> predicate);	
	
	IEntity findByKey(String key);
	/**
	 * Generate unique id.
	 * @return 64bit id unique in the model
	 */
	long generateId();
	
	int generateProbeId();
	
	long generateTaskId();
	boolean setModelIdPrefix(long prefix, boolean force);
	
	ITaskRecord findTaskById(long taskId);
	
	List<ITaskRecord> findTaskByProbeId(long probeId);
	
	IEntityReference createEntityReference(IEntity entity);
	
	ISpace createSpace(IEntity root, IProbe probe);
	Set<ISpace> getOpenSpaces();
	Set<ISpace> getAllSpaces();
	ISpace findSpaceById(long id);
	void addSpaceCreationListener(IEventHandler handler);
	void removeSpaceCreationListener(IEventHandler handler);

	IModelService getModel();

	/**
	 * One more than the highest index in the update table.  If the update table is empty this value is 0.
	 * @return
	 */
	long getCurrentUpdateIndex();

	void addEntityUpdateListener(IEventHandler handler);
	void removeEntityUpdateListener(IEventHandler handler);
}
