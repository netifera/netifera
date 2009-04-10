package com.netifera.platform.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.db4o.ObjectContainer;
import com.db4o.ext.DatabaseClosedException;
import com.netifera.platform.api.events.EventListenerManager;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.api.model.SpaceNameChangeEvent;
import com.netifera.platform.api.model.SpaceTaskChangeEvent;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tasks.ITaskRecord;
import com.netifera.platform.api.tasks.ITaskStatus;
import com.netifera.platform.model.ProbeEntity;

public class Space implements ISpace {
	private final static int BACKGROUND_COMMIT_INTERVAL = 5000;
	/* Unique ID value for this space */
	private final long id;
	
	/* Name for this space to display in the user interface */
	private String name;
	
	/* Every space has a root entity which is a realm creating entity.  Generally this is a probe entity */
	private final IEntity rootEntity;
	
	/* Every space is permanently associated with a single Probe entity */
	private final ProbeEntity probeEntity;
	
	/* The list of entities that are contained in this space. */
	private final List<IEntity> spaceEntities;
	
	/* The list of tasks which have been executed in this space */
	
	private final List<ITaskRecord> spaceTasks;
	private final SpaceManager manager;
	private transient boolean isOpened;
	
	/* Optimization to quickly test if an entity is present in this view */
	private transient Set<IEntity> entitySet;

	private transient EventListenerManager spaceChangeListeners;
	
	private transient EventListenerManager taskChangeListeners;
	
	private transient Thread commitThread;
	private transient boolean entitiesDirty;
	private transient boolean tasksDirty;
	private transient ObjectContainer database;
	private transient ILogger logger;
	
	/* Create a new space */
	Space(long id, IProbe probe, String name, IEntity root, SpaceManager manager) {
		this.id = id;
		this.probeEntity = (ProbeEntity) probe.getEntity();
		this.name = name;
		this.rootEntity = root;
		this.spaceEntities = Collections.synchronizedList(new ArrayList<IEntity>());
		this.spaceTasks = Collections.synchronizedList(new ArrayList<ITaskRecord>());
		this.entitySet = Collections.synchronizedSet(new HashSet<IEntity>());
		this.manager = manager;
		this.database = manager.getDatabase();
		this.logger = manager.getLogger().getManager().getLogger("Space : " + name);
		startCommitThread();
	}
	
	public void objectOnActivate(ObjectContainer container) {
		this.database = container;
		buildEntitySet();
		startCommitThread();
	}
	
	public void open() {
		isOpened = true;
		manager.openSpace(this);
	}
	
	public void close() {
		isOpened = false;
		manager.closeSpace(this);
	}
	
	public boolean isOpened() {
		return isOpened;
	}
	
	ObjectContainer getDatabase() {
		return database;
	}
	
	public List<IEntity> getEntities() {
		return Collections.unmodifiableList(spaceEntities);
	}
	
	public synchronized void addEntity(IEntity entity) {
		if(!entitySet.contains(entity)) {
			entitySet.add(entity);
			spaceEntities.add(entity);
			entitiesDirty = true;
			getEventManager().fireEvent(SpaceContentChangeEvent.createCreationEvent(entity));
			manager.notifySpaceChange(this);
		}
	}
	
	public void updateEntity(IEntity entity) {	
		if(entitySet.contains(entity)) {
			getEventManager().fireEvent(SpaceContentChangeEvent.createUpdateEvent(entity));
		}
	}
	
	public synchronized void removeEntity(IEntity entity) {
		if(entitySet.contains(entity)) {
			entitySet.remove(entity);
			spaceEntities.remove(entity);
			entitiesDirty = true;
			getEventManager().fireEvent(SpaceContentChangeEvent.createRemovalEvent(entity));
			manager.notifySpaceChange(this);
		}
	}
	
	public Set<String> getTags() {
		Set<String> tags = new HashSet<String>();
		for (IEntity entity: entitySet) {
			tags.addAll(entity.getTags());
		}
		return tags;
	}
	
	public synchronized void addTaskRecord(ITaskStatus status) {
		final TaskRecord record = new TaskRecord(status, this);
		if(spaceTasks.contains(record)) {
			return;
		}
		spaceTasks.add(record);
		database.store(record);
		manager.addTaskToSpace(status.getTaskId(), this);
		tasksDirty = true;
		getTaskEventManager().fireEvent(SpaceTaskChangeEvent.createCreationEvent(record));
		
	}
	
	public void updateTaskRecord(ITaskRecord record) {
		getTaskEventManager().fireEvent(SpaceTaskChangeEvent.createUpdateEvent(record));
	}
	
	public List<ITaskRecord> getTasks() {
		return Collections.unmodifiableList(spaceTasks);
	}
	
	
	private void buildEntitySet() {
		entitySet = new HashSet<IEntity>();
		for(IEntity entity : spaceEntities) {
			entitySet.add(entity);
		}
	}
	
	public IWorkspace getWorkspace() {
		return rootEntity.getWorkspace();
	}
	
	public IEntity getRootEntity() {
		return rootEntity;
	}
	
	public long getId() {
		return id;
	}

	public long getProbeId() {
		return probeEntity.getProbeId();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		database.store(this);
		manager.notifySpaceChange(this);
		getEventManager().fireEvent(new SpaceNameChangeEvent(name));
	}
	
	public int entityCount() {
		return spaceEntities.size();
	}
	
	public void addChangeListenerAndPopulate(IEventHandler handler) {
		getEventManager().addListener(handler);
		synchronized(spaceEntities) {
			for(IEntity entity : spaceEntities) {
				handler.handleEvent(SpaceContentChangeEvent.createCreationEvent(entity));
			}
		}
	}
	
	public void addChangeListener(IEventHandler handler) {
		getEventManager().addListener(handler);		
	}

	public void removeChangeListener(IEventHandler handler) {
		getEventManager().removeListener(handler);		
	}
	
	private EventListenerManager getEventManager() {
		if(spaceChangeListeners == null) {
			spaceChangeListeners = new EventListenerManager();
		}
		return spaceChangeListeners;
	}
	
	public void addTaskChangeListenerAndPopulate(IEventHandler handler) {
		getTaskEventManager().addListener(handler);
		for(ITaskRecord task : spaceTasks) {
			handler.handleEvent(SpaceTaskChangeEvent.createCreationEvent(task));
		}
	}
	
	public void addTaskChangeListener(IEventHandler handler) {
		getTaskEventManager().addListener(handler);
	}
	
	public void removeTaskChangeListener(IEventHandler handler) {
		getTaskEventManager().removeListener(handler);
	}
	
	private EventListenerManager getTaskEventManager() {
		if(taskChangeListeners == null) {
			taskChangeListeners = new EventListenerManager();
		}
		return taskChangeListeners;
	}
	
	public String toString() {
		return "Space (" + name + ") " + (isOpened ? "[opened]" : "[closed]");
	}
	
	public boolean equals(Object other) {
		if(!(other instanceof Space)) {
			return false;
		}
		return ((Space)other).getId() == id;
	}
	
	public int hashCode() {
		return (int) id;
	}
	
	private void startCommitThread() {
		commitThread = new Thread(new Runnable() {

			public void run() {
				while(true) {
					try {
						Thread.sleep(BACKGROUND_COMMIT_INTERVAL);
						if(database.ext().isClosed())
							return;
						runCommit();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return;
					} catch (DatabaseClosedException e) {
						return;
					}
				}				
			}
			
		});
		commitThread.setDaemon(true);
		commitThread.setName("Background Commit thread for space [" + name + "]");
		commitThread.start();
	}
	
	

	private synchronized void runCommit() {		
		if(tasksDirty) {
			commitTasks();
			tasksDirty = false;
		}
		
		if(entitiesDirty) {
			commitEntities();
			entitiesDirty = false;
		}	
	}
	
	private void commitEntities() {
		synchronized(spaceEntities) {
			database.store(spaceEntities);
		}
		
	}
	private void commitTasks() {
		synchronized (spaceTasks) {
			database.store(spaceTasks);
		}

	}

}
