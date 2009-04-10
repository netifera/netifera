package com.netifera.platform.ui.spaces.editors;

import java.io.Serializable;
import java.util.Comparator;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IShadowEntity;


public class EntityComparator implements Serializable,
		Comparator<IShadowEntity> {
	private static final long serialVersionUID = 524197591393448269L;

	@SuppressWarnings("unchecked")
	public int compare(IShadowEntity s1, IShadowEntity s2) {
		if (s1.equals(s2)) {
			return 0;
		}
		
		IEntity e1 = s1.getRealEntity();
		IEntity e2 = s2.getRealEntity();
		if (e1.equals(e2)) {
			return 0;
		}
		
		if (e1.getClass() != e2.getClass()) {
			/* not the same kind of entities */
			return e1.getTypeName().compareTo(e2.getTypeName());
		}

		if (e1 instanceof Comparable) {
			((Comparable<IEntity>)e1).compareTo(e2);
		}

		return -1;
	}

}
