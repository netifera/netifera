package com.netifera.platform.internal.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.db4o.ObjectContainer;
import com.netifera.platform.api.events.EventListenerManager;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.probe.IProbe;

public class SpaceManager {
	private long currentSpaceId;
	private transient Set<ISpace> openSpaces;
	private Set<ISpace> allSpaces;
	private Map <Long, ISpace> taskIdToSpace;
	private Map <Long, ISpace> spaceIdToSpace;
	private transient ObjectContainer database;
	private transient Workspace workspace;
	private transient EventListenerManager changeListeners;
	private transient ILogger logger;


	public static SpaceManager getSpaceManager(final ObjectContainer db, final Workspace workspace) {
		final List<SpaceManager> result = db.query(SpaceManager.class);
		if(result.isEmpty()) {
			return new SpaceManager(db, workspace);
		} else if(result.size() == 1) {
			return result.get(0).initialize(db, workspace);
		} else {
			throw new IllegalStateException("Multiple SpaceManager objects found in database");
		}
		
	}
	
	ObjectContainer getDatabase() {
		return database;
	}
	
	private SpaceManager(ObjectContainer db, Workspace workspace) {
		initialize(db, workspace);
		allSpaces = new HashSet<ISpace>();
		taskIdToSpace = new HashMap<Long, ISpace>();
		spaceIdToSpace = new HashMap<Long, ISpace>();
		commit();
	}	

	private SpaceManager initialize(ObjectContainer db, Workspace workspace) {
		this.database = db;
		this.workspace = workspace;
		this.openSpaces = new HashSet<ISpace>();
		if(workspace != null) {
			logger = workspace.getLogger();
		}
		return this;
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	public ILogger getLogger() {
		return logger;
	}
	
	public ISpace findSpaceById(long id) {
		return spaceIdToSpace.get(id);
	}
	
	public Set<ISpace> getAllSpaces() {
		return Collections.unmodifiableSet(allSpaces);
	}
	
	public synchronized Set<ISpace> getOpenSpaces() {
		return Collections.unmodifiableSet(openSpaces);
	}
	
	public synchronized ISpace findSpaceForTaskId(long taskId) {
		return taskIdToSpace.get(taskId);
	}
	
	public synchronized void notifySpaceChange(ISpace space) {
		fireSpaceChangeEvent(space);
	}
	
	public synchronized void addTaskToSpace(long taskId, ISpace space) {
		taskIdToSpace.put(taskId, space);
		database.store(taskIdToSpace);
		fireSpaceChangeEvent(space);
	}
	
	synchronized void openSpace(ISpace space) {		
		openSpaces.add(space);
		fireSpaceChangeEvent(space);
	}
	
	synchronized void closeSpace(ISpace space) {
		openSpaces.remove(space);
		fireSpaceChangeEvent(space);
	}
		
	
	public synchronized ISpace createSpace(IEntity root, IProbe probe) {
		final long id = generateNewViewId();
		final ISpace space = new Space(id, probe, "Space " + id, root, this);
		database.store(space);
		allSpaces.add(space);
		spaceIdToSpace.put(space.getId(), space);
		database.store(allSpaces);
		database.store(spaceIdToSpace);
		commit();
		getEventManager().fireEvent(SpaceStatusChangeEvent.createNewEvent(space)); 
		return space;
	}
	
	private synchronized long generateNewViewId() {
		currentSpaceId += 1;
		commit();
		return currentSpaceId;
	}
	
	private void commit() {	
		database.store(this);
	}
	
	private void fireSpaceChangeEvent(ISpace space) {
		getEventManager().fireEvent(SpaceStatusChangeEvent.createChangedEvent(space));
	}
	
	public void addChangeListener(IEventHandler handler) {
		getEventManager().addListener(handler);
	}
	
	public void removeChangeListener(IEventHandler handler) {
		getEventManager().removeListener(handler);
	}
	
	private EventListenerManager getEventManager() {
		if(changeListeners == null) {
			changeListeners = new EventListenerManager();
		}
		return changeListeners;
	}

}
