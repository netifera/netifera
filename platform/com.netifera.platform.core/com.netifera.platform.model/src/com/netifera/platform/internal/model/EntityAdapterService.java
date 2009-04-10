package com.netifera.platform.internal.model;

import java.util.LinkedList;
import java.util.List;

import com.netifera.platform.api.iterables.IndexedIterable;
import com.netifera.platform.api.iterables.SingleElementIndexedIterable;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityAdapterProvider;
import com.netifera.platform.api.model.IEntityAdapterService;

public class EntityAdapterService implements IEntityAdapterService {
	final private List<IEntityAdapterProvider> providers = new LinkedList<IEntityAdapterProvider>();

	protected void registerEntityAdapterProvider(final IEntityAdapterProvider provider) {
		providers.add(provider);
	}

	protected void unregisterEntityAdapterProvider(final IEntityAdapterProvider provider) {
		providers.remove(provider);
	}

	public Object getAdapter(final IEntity entity, final Class<?> adapterType) {
		for (IEntityAdapterProvider provider : providers) {
			Object adapter = provider.getAdapter(entity, adapterType);
			if (adapter != null)
				return adapter;
		}
		return null;
	}
	
	public IndexedIterable<?> getIterableAdapter(final IEntity entity, final Class<?> iterableType) {
		for (IEntityAdapterProvider provider: providers) {
			IndexedIterable<?> adapter = provider.getIterableAdapter(entity, iterableType);
			if (adapter != null)
				return adapter;
		}
		
		Object element = getAdapter(entity, iterableType);
		if (element != null)
			return new SingleElementIndexedIterable<Object>(element);
		
		return null;
	}
}
