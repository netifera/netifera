package com.netifera.platform.api.model;

import com.netifera.platform.api.iterables.IndexedIterable;

public interface IEntityAdapterProvider {
//	Class<?>[] getAdapterList();
	Object getAdapter(IEntity entity, Class<?> adapterType);
	IndexedIterable<?> getIterableAdapter(IEntity entity, Class<?> iterableType);
}
