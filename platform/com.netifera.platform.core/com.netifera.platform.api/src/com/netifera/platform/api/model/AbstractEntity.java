package com.netifera.platform.api.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.netifera.platform.api.iterables.IndexedIterable;


public abstract class AbstractEntity implements IEntity, IShadowEntity, Serializable {
	
	private static final long serialVersionUID = 5475428943049177850L;

	/* A string describing the type of this entity */
	private final String typeName;
	
	/* The workspace instance this entity was created in */
	private transient IWorkspace workspace;
	
	private String queryKey;
	/* 
	 * The id value of this entity instance.  If the id is 0, then
	 * the entity has not yet been stored permanently in the model.
	 */
	private long id = 0;
	
	/* The entity id of the 'realm' this entity belongs to */
	private long realmId;
	
	private Map<String, IEntityReference> namedAssociations;
	private Map<String, String> namedAttributes;
	
	private Set<String> tags;

	/**
	 * 
	 * @param typeName A string describing the type of this entity.
	 * @param workspace The workspace instance this entity was created in.
	 * @param realmId The entity id of the realm this entity belongs to.
	 */
	protected AbstractEntity(final String typeName, final IWorkspace workspace, final long realmId) {
		this.typeName = typeName;
		this.workspace = workspace;
		this.realmId = realmId;
	}
	
	protected AbstractEntity() {
		this.typeName = null;
		this.workspace = null;
	}
	
	private Map<String, IEntityReference> getNamedAssociationsMap() {
		if(namedAssociations == null) {
			namedAssociations = new HashMap<String, IEntityReference>();
		}
		return namedAssociations;
	}
	
	private Map<String, String> getNamedAttributesMap() {
		if(namedAttributes == null) {
			namedAttributes = new HashMap<String, String>();
		}
		return namedAttributes;
	}

	private Set<String> getTagsSet() {
		if(tags == null) {
			tags = new HashSet<String>();
		}
		return tags;
	}
	
	public void setWorkspace(final IWorkspace workspace) {
		this.workspace = workspace;
	}
		
	public void setNamedAttribute(final String name, final String value) {
		getNamedAttributesMap().put(name, value);
	}

	public String getNamedAttribute(final String name) {
		return getNamedAttributesMap().get(name);
	}

	public void setNamedAssociation(String name, IEntity value) {
		getNamedAssociationsMap().put(name, value.createReference());
	}

	public IEntity getNamedAssociation(final String name) {
		IEntityReference ref = getNamedAssociationsMap().get(name);
		if (ref == null) return null;
		return referenceToEntity(ref);
	}
	
	public String getTypeName() {
		return typeName;
	}

	public long privateGetId() {
		return id;
	}
	
	public long getId() {
		if(id == 0) {
			throw new IllegalStateException("IEntity#getId() called on an entity which has not been saved: "+this);
		}
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public void setRealmId(long id) {
		this.realmId = id;
	}
	public IEntityReference createReference() {
		if(id == 0) {
			throw new IllegalStateException("IEntity#createReference called on an entity which has not been saved.");
		}
		return workspace.createEntityReference(this);
	}
	
	protected IEntity referenceToEntity(final IEntityReference reference) {
		if(reference == null) {
// uncomment to see when this conditions holds...
// otherwise we could just do reference.get() and eliminate this method
/* 			try {
 
				throw new Exception();
			} catch (Exception e) {
				e.printStackTrace();
			}
*/			return null;
		}
		return reference.getEntity(workspace);
	}

	/**
	 * Override this to return false for pseudo-entities that should not be saved in the database.
	 * @return true if this entity may be saved in the database, false otherwise.
	 */
	protected boolean canSave() {
		return true;
	}
	
	protected String generateQueryKey() {
		return "";
	}
	
	public String getQueryKey() {
		return queryKey;
	}
	
	/**
	 * Save entity information in database without notifying listeners
	 */
	public synchronized void save() {
		if(!canSave()) {
			throw new IllegalStateException("IEntity#save() called on entity that cannot be saved.");
		}
		
		if(id == 0) {
			id = workspace.generateId();
		}
		queryKey = generateQueryKey();
		workspace.storeEntity(this);
	}
	
	public synchronized void addToSpace(long spaceId) {
		if(id == 0) {
			throw new IllegalStateException("Cannot add entity to space until it has been saved");
		}
		workspace.addEntityToSpace(this, spaceId);
	}
	
	
	public void updateFromEntity(IEntity entity) {
		if(!this.getClass().isInstance(entity)) {
			throw new IllegalArgumentException();			
		}
		synchronized(this) {
			synchronizeEntity((AbstractEntity) entity);
			synchronizeTags((AbstractEntity) entity);
			synchronizeAttributesAndAssociations((AbstractEntity) entity);
		}
		update();
	}
		
	public synchronized void update() {
		if(!canSave()) {
			throw new IllegalStateException("IEntity#update() called on an entity that cannot be saved.");
		}
		
		if(id == 0) {
			throw new IllegalStateException("IEntity#update() called on an entity which has not previously been saved.");
		}
				
		synchronizeShadowEntities();
	
		queryKey = generateQueryKey();
		workspace.updateEntity(this);
	}
	
	private void synchronizeShadowEntities() {
		synchronized(getShadowLock()) {
			if(shadowEntities == null) {
				return;
			}
			for(IShadowEntity shadow : shadowEntities) {
				((AbstractEntity)shadow).synchronizeAttributesAndAssociations(this);
				((AbstractEntity)shadow).synchronizeTags(this);
				((AbstractEntity)shadow).synchronizeEntity(this);
			}
		}
	}
	
	public IWorkspace getWorkspace() {
		return workspace;
	}
	
	public long getRealmId() {
		return realmId;
	}
	
	public boolean isRealmEntity() {
		return false;
	}

	/*
	 * Shadow entity handling below this line
	 * 
	 */

	private static class ShadowEntityContext {
		private IStructureContext structureContext;
		/* used by shadow entities to reference the entity they were cloned from */
		private AbstractEntity originalEntity;
		private ISpace view;
	}
	
	/*
	 * This field is always null for normal entities, it is a valid reference
	 * if and only if this is a shadow entity instance
	 */
	private transient ShadowEntityContext shadowContext;
	private transient Object shadowLock = new Object();

	private transient List<IShadowEntity> shadowEntities;
	
	private synchronized Object getShadowLock() {
		if(shadowLock == null) {
			shadowLock = new Object();
		}
		return shadowLock;
	}
	
	private void addShadowEntity(final AbstractEntity entity, final IStructureContext structure) {
	
		synchronized(getShadowLock()) {
			if(shadowEntities == null) {
				shadowEntities = new LinkedList<IShadowEntity>();
			}
			
			entity.shadowContext = new ShadowEntityContext();
			entity.shadowContext.originalEntity = this;
			entity.shadowContext.structureContext = structure;
		
			shadowEntities.add(entity);
		}
	}
	
	protected abstract IEntity cloneEntity();
	
	protected void synchronizeEntity(AbstractEntity masterEntity) {
		// subclass responsibility
	}

	private void synchronizeTags(AbstractEntity entity) {
		if(entity.tags == null) 
			return;
		for(String tag : entity.tags) {
			addTag(tag);
		}
		//FIXME what if a tag was removed?
	}

	private void synchronizeAttributesAndAssociations(AbstractEntity masterEntity) {
		if(masterEntity.namedAttributes != null) {
			mergeNamedAttributes(masterEntity.namedAttributes, getNamedAttributesMap());
		}
		
		if(masterEntity.namedAssociations != null) {
			mergeNamedAssociations(masterEntity.namedAssociations, getNamedAssociationsMap());	
		}	
	}
	
	/* These merge methods replace old information with new information */
	private void mergeNamedAttributes(Map<String, String> from, Map<String,String> to) {
		for(String key : from.keySet()) 
			to.put(key, from.get(key));
		//FIXME what if an attribute was removed?
	}
	
	private void mergeNamedAssociations(Map<String, IEntityReference> from, Map<String, IEntityReference> to) {
		for(String key : from.keySet()) 
			to.put(key, from.get(key));	
		//FIXME what if an association was removed?
	}
	
	public IShadowEntity shadowClone(final IStructureContext structure) {
		if(structure == null) {
			throw new NullPointerException();
		}
		AbstractEntity clone = (AbstractEntity) cloneEntity();
		clone.synchronizeTags(this);
		clone.synchronizeAttributesAndAssociations(this);
		clone.id = id;

		addShadowEntity(clone, structure);
		return clone;
	}
	
	public IEntity getRealEntity() {
		/*
		 * If there is no shadow context, then this is the real entity
		 * not a shadow entity.
		 */
		if(shadowContext == null) {
			return this;
		}
		
		return shadowContext.originalEntity;
	}

	public IStructureContext getStructureContext() {
		if(shadowContext == null) {
			throw new IllegalStateException();
		}
		return shadowContext.structureContext;
	}
	
	public IShadowEntity searchEntity(IEntity entity) {
		if(shadowContext == null) {
			throw new IllegalStateException();
		}
		return shadowContext.structureContext.searchEntity(entity);		
	}
	
	public void dispose() {
		if(shadowContext == null) {
			throw new IllegalStateException();
		}
		shadowContext.originalEntity.disposeShadow(this);
	}
	
	private synchronized void disposeShadow(IShadowEntity entity) {
		if(shadowEntities == null) {
			throw new IllegalStateException();
		}
		shadowEntities.remove(entity);
		if(shadowEntities.isEmpty()) {
			shadowEntities = null;
		}
	}
	
	public synchronized Set<String> getTags() {
		if (tags == null)
			return Collections.emptySet();
		else
			return Collections.unmodifiableSet(tags);
	}

	public synchronized void addTag(String tag) {
		getTagsSet().add(tag);
	}

	public synchronized void removeTag(String tag) {
		if (tags == null) return;
		tags.remove(tag);
	}

	public long getViewId() {
		if(shadowContext == null) {
			throw new IllegalStateException();
		}
		return shadowContext.view.getId();
	}
	
	public Object getAdapter(final Class<?> adapterType) {
		if (workspace == null) return null;
		return workspace.getModel().getAdapterService().getAdapter(this, adapterType);
	}	
	
	public IndexedIterable<?> getIterableAdapter(final Class<?> iterableType) {
		if (workspace == null) return null;
		return workspace.getModel().getAdapterService().getIterableAdapter(this, iterableType);
	}
	
	public String toString() {
		return "Entity [" + typeName + "] id = " + id + " realm id = " + realmId;
	}
}
