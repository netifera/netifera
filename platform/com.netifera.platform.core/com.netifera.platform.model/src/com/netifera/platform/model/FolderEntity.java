package com.netifera.platform.model;

import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.api.iterables.IndexedIterable;
import com.netifera.platform.api.iterables.ListIndexedIterable;
import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IShadowEntity;

public class FolderEntity extends AbstractEntity implements Comparable<FolderEntity> {
	
	private static final long serialVersionUID = -4274797397989903181L;

	public final static String ENTITY_NAME = "folder";
	
	private final String contentType;
	private final String tag;

	public FolderEntity(long realm, String contentType, String tag) {
		super(ENTITY_NAME, null, realm);
		this.contentType = contentType;
		this.tag = tag;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public String getTag() {
		return tag;
	}

	public String getLabel() {
		return tag;
	}
	
	protected IEntity cloneEntity() {
		// is this safe?
		return this;
	}
	
	@Override
	public IndexedIterable<?> getIterableAdapter(final Class<?> iterableType) {
		if (!(getStructureContext() instanceof TreeStructureContext))
			return null;

		List<Object> list = new ArrayList<Object>();
		for (IShadowEntity child: ((TreeStructureContext)getStructureContext()).getChildren()) {
			Object adapter = child.getRealEntity().getAdapter(iterableType);
			if (adapter != null)
				list.add(adapter);
		}
		if (list.size() == 0)
			return null;
		return new ListIndexedIterable<Object>(list);
	}
	
	public int compareTo(FolderEntity other) {
		int r;
		if (contentType != null && other.contentType != null) {
			r = contentType.compareTo(other.contentType);
			if (r != 0) {
				return r > 0 ? 1 : -1;
			}
		}
		r = tag.compareTo(other.tag);
		return r > 0 ? 1 : (r < 0 ? -1 : 0);
	}
}
