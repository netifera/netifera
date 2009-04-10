package com.netifera.platform.internal.model;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.events.Event4;
import com.db4o.events.EventArgs;
import com.db4o.events.EventListener4;
import com.db4o.events.EventRegistry;
import com.db4o.events.EventRegistryFactory;
import com.db4o.events.ObjectEventArgs;
import com.db4o.ext.DatabaseFileLockedException;
import com.db4o.query.Predicate;
import com.netifera.platform.api.events.EventListenerManager;
import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IModelPredicate;
import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tasks.ITaskRecord;
import com.netifera.platform.model.IUpdateRecord;
import com.netifera.platform.model.IWorkspaceEx;

public class Workspace implements IWorkspaceEx {
	public final static int MODEL_VERSION = 1;
	private final static int BACKGROUND_COMMIT_INTERVAL = 120000;

	private final ModelService model;
	private ObjectContainer database;
	private ModelStatus status;
	private SpaceManager spaceManager;
	private QueryProcessor queryProcessor;
	private TaskManager taskManager;
	private UpdateTable updateTable;
	private boolean opened;
	private ILogger logger;
	private final String databaseFilePath;
	private Thread commitThread;
	
	private transient EventListenerManager entityUpdateListeners;
	
	
	Workspace(ModelService model, String path) {
		this.model = model;
		this.databaseFilePath = path;
		this.opened = false;
		logger = model.getLogger();
	}
	
	public boolean open() {
		try {
			database = model.getDatabaseFactory().openContainer(databaseFilePath);
			openDatabase();
			return true;
		} catch(DatabaseFileLockedException e) {
			logger.error("Database '" + databaseFilePath + "' is locked by another process");
			return false;
		} catch(ModelVersionException e) {
			logger.error("Database '" + databaseFilePath + "' is incompatible with this version of netifera.");
			return false;
		} catch(Exception e) {
			logger.error("Open workspace database '" + databaseFilePath + "' failed.", e);
			return false;
		}
	}
	
	private void openDatabase() {
		registerActivationCallback();
		status = ModelStatus.getModelStatus(database);
		if(status.getModelVersion() > MODEL_VERSION || status.getModelVersion() < 1) 
			throw new ModelVersionException();
		spaceManager = SpaceManager.getSpaceManager(database, this);
		queryProcessor = new QueryProcessor(database, model);
		taskManager = new TaskManager(database, model);
		updateTable = UpdateTable.getUpdateTable(database, this);
		opened = true;
		logger.info("Loaded workspace from " + databaseFilePath);
		startCommitThread();
	}
	
	ILogger getLogger() {
		return logger;
	}
	private void registerActivationCallback() {
		/* inject a reference to the workspace in each newly instantiated entity */
		EventRegistry registry = EventRegistryFactory.forObjectContainer(database);
		registry.activating().addListener(new EventListener4() {

			public void onEvent(Event4 event, EventArgs args) {
				if (args instanceof ObjectEventArgs) {
					Object ob = ((ObjectEventArgs)args).object();
					if(ob instanceof AbstractEntity) {
						AbstractEntity e = (AbstractEntity)ob;
						e.setWorkspace(Workspace.this);
					} 
				}
			}
			
		});
	}
	public void close() {
		if(!opened) 
			return;
		
		if(commitThread != null) {
			commitThread.interrupt();
			commitThread = null;
		}
		database.close();
		opened = false;
		
	}
	
	public <T extends IEntity> void storeEntity(T entity) {
		database.store(entity);
	}
	
	public void addEntityToSpace(IEntity entity, long spaceId) {
		for(ISpace space : spaceManager.getAllSpaces()) {
			if(space.getId() == spaceId) {
				space.addEntity(entity);
			}
		}
		updateTable.addEntityToSpace(entity, spaceId);
		fireEntityUpdate();
	}
	
	public <T extends IEntity> void updateEntity(T entity) {
		storeEntity(entity);
		for(ISpace space : spaceManager.getOpenSpaces()) {
			space.updateEntity(entity);
		}
		updateTable.updateEntity(entity);
		fireEntityUpdate();
	}
	
	public <T extends IEntity> List<T> findAll(Class<T> klass) {
		return queryProcessor.findAll(klass);
	}
	
	public IEntity findById(long id) {
		return queryProcessor.findById(id);
	}
	
	public List<IEntity> findByRealm(long realm) {
		return queryProcessor.findByRealm(realm);
	}
	
	public <T extends IEntity> List<T> findByPredicate(IModelPredicate<T> predicate) {
		return queryProcessor.findByPredicate(predicate);
	}
	
	public <T extends IEntity> List<T> findByPredicate(IModelPredicate<T> predicate, Comparator<T> comparator) {
		return queryProcessor.findByPredicate(predicate, comparator);
	}
	
	public <T extends IEntity> List<T> findByPredicate(Class<T> klass, IModelPredicate<T> predicate) {
		return queryProcessor.findByPredicate(klass, predicate);
	}
	
	@SuppressWarnings("serial")
	public IEntity findByKey(final String key) {
		ObjectSet<AbstractEntity> result = database.query(new Predicate<AbstractEntity>() {

			@Override
			public boolean match(AbstractEntity candidate) {
				return candidate.getQueryKey().equals(key);
			}
			
		});
		if(result.size() == 0) {
			return null;
		} else if(result.size() == 1) {
			return result.get(0);
		} else {
			logger.error("Database corrupted, multiple results returned for key: " + key);
			return null;
		}
	}
	
	public IEntityReference createEntityReference(IEntity entity) {
		return EntityReference.create(entity.getId());
	}
		
	
	public ITaskRecord findTaskById(long taskId) {
		return taskManager.findTaskById(taskId);
	}
	
	public List<ITaskRecord> findTaskByProbeId(long probeId) {
		return taskManager.findTaskByProbeId(probeId);
	}
	
	public ISpace createSpace(IEntity root, IProbe probe) {
		return spaceManager.createSpace(root, probe);
	}
	
	public Set<ISpace> getOpenSpaces() {	
		return spaceManager.getOpenSpaces();
	}
	
	public Set<ISpace> getAllSpaces() {
		return spaceManager.getAllSpaces();
	}
	
	public ISpace findSpaceById(long id) {
		return spaceManager.findSpaceById(id);
	}
	
	public void addSpaceCreationListener(IEventHandler handler) {
		spaceManager.addChangeListener(handler);
	}
	
	public void removeSpaceCreationListener(IEventHandler handler) {
		spaceManager.removeChangeListener(handler);
	}

	public long generateId() {
		return status.generateEntityId();
	}
	
	
	
	public long getCurrentUpdateIndex() {
		return updateTable.getCurrentUpdateIndex();
	}
	
	public IUpdateRecord getEntityByUpdateIndex(long updateIndex) {
		final UpdateRecord record = updateTable.getUpdateElement((int) updateIndex);
		return record.getTransferRecord();
	}
	
	public int generateProbeId() {
		return status.generateProbeId();
	}
	
	public long generateTaskId() {
		return status.generateTaskId();
	}
	
	public boolean setModelIdPrefix(long prefix, boolean force) {
		if(status.getIdPrefix() == prefix) {
			return true;
		}
		if(!status.setIdPrefix(prefix, force)) {
			return false;
		}
		reindexModel(prefix);
		return true;
	}
	
	private void reindexModel(long prefix) {
		List<AbstractEntity> all = findAll(AbstractEntity.class);
		for(AbstractEntity entity : all) {
			long oldId = entity.getId();
			entity.setId( prefix << 32 | (oldId & 0xFFFFFFFF));
			entity.save();
		}
		
		// XXX tasks?
		
	}
	
	public IModelService getModel() {
		return model;
	}
	
	
	private void fireEntityUpdate() {
		getEventManager().fireEvent(new IEvent(){} );
	}
	public void addEntityUpdateListener(IEventHandler handler) {
		getEventManager().addListener(handler);
	}
	
	public void removeEntityUpdateListener(IEventHandler handler) {
		getEventManager().removeListener(handler);
	}
	private EventListenerManager getEventManager() {
		if(entityUpdateListeners == null) {
			entityUpdateListeners = new EventListenerManager();
		}
		return entityUpdateListeners;
	}

	public ObjectContainer getRawDatabase() {
		return database;
	}
	
	private synchronized void startCommitThread() {
		commitThread = new Thread(new Runnable() {

			public void run() {
				while(!Thread.currentThread().isInterrupted()) {
					try {
						Thread.sleep(BACKGROUND_COMMIT_INTERVAL);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return;
					}
					runCommit();
				}				
			}
			
		});
		
		commitThread.setDaemon(true);
		commitThread.setName("Background Commit Workspace");
		commitThread.start();
		
	}
	
	private void runCommit() {
		try {
			database.commit();
		} catch(RuntimeException e) {
			Thread.currentThread().interrupt();
		}
	}

	
}
