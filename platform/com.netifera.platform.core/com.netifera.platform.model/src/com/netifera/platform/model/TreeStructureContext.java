package com.netifera.platform.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.model.IStructureContext;

public class TreeStructureContext implements IStructureContext {
	private IShadowEntity parent;
	private IShadowEntity entity;
	private List<IShadowEntity> children;
	
	/*
	 * XXX need to add disposal methods 
	 */
	public static IShadowEntity createRoot(IEntity entity) {
		return createNode(entity);
	}
	
	public IShadowEntity getParent() {
		return parent;
	}
	
	public IShadowEntity getEntity() {
		return entity;
	}
	
	public IShadowEntity addChild(IEntity entity) {
		synchronized(this) {
			if(children == null) {
				children = Collections.synchronizedList(new ArrayList<IShadowEntity>());
			}
		}
		
		IShadowEntity shadow = createNode(entity, this.entity);
		children.add(shadow);
		return shadow;
	}
	
	public IShadowEntity removeChild(IEntity entity) {
		if (children == null) return null;
		synchronized(children) {
			return removeChildEntity(entity);
		}	
	}
	
	private IShadowEntity removeChildEntity(IEntity entity) {
		for (int i=0; i<children.size(); i++) {
			if (children.get(i).getRealEntity() == entity) {
				IShadowEntity shadow = children.remove(i);
				shadow.dispose();
				return shadow;
			}
		}
		return null;
	}

	public boolean hasChildren() {
		return (children != null && children.size() > 0);
	}
	
	public List<IShadowEntity> getChildren() {
		List<IShadowEntity> retChildren;
		
		if(children == null) {
			return Collections.emptyList();
		}
		/*return a copy to avoid concurrent modification exceptions */
		synchronized(children) {
			retChildren = new ArrayList<IShadowEntity>(children); 
		}
		return retChildren;
	}

	public boolean hasChild(IEntity entity) {
		return getChild(entity) != null;
	}

	private IShadowEntity getChild(IEntity entity) {
		if (children == null) {
			return null;
		} else synchronized(children) {
			return getChildById(entity.getId());
		}
	}
	
	private IShadowEntity getChildById(long id) {
		for (IShadowEntity child: children)
			if (child.getRealEntity().getId() == id)
				return child;
		return null;
	}

	public boolean isRoot() {
		return parent == null;
	}
	
	public IShadowEntity searchEntityById(final long entityId) {
		if(entity.getRealEntity().getId() == entityId) return entity;
		
		if(children == null) {
			return null;
		} else synchronized(children) {
			return searchChildrenById(entityId);
		}
	}
	
	private IShadowEntity searchChildrenById(long entityId) {
		for(IShadowEntity child: children) {
			IStructureContext ctx = child.getStructureContext();
			IShadowEntity found = ctx.searchEntityById(entityId);
			if(found != null) return found;
		}
		return null;
	}
	
	public IShadowEntity searchEntity(IEntity target) {
		if(entity.getRealEntity() == target) return entity;
		
		if(children == null) {
			return null;
		} else synchronized(children) {
			return searchChildren(target);
		}
	}
	
	private IShadowEntity searchChildren(IEntity target) {
		for(IShadowEntity child: children) {
			IShadowEntity found = child.searchEntity(target);
			if(found != null) return found;
		}
		return null;
	}

	private static IShadowEntity createNode(IEntity entity) {
		return createNode(entity, null);
	}
	
	private static IShadowEntity createNode(IEntity entity, IShadowEntity parent) {
		TreeStructureContext tsc = new TreeStructureContext();
		tsc.entity = entity.shadowClone(tsc);
		tsc.parent = parent;
		return tsc.getEntity();
	}
}
