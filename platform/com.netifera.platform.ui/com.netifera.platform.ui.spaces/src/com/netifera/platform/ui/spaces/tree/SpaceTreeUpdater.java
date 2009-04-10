package com.netifera.platform.ui.spaces.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;

import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.ISpaceContentChangeEvent;
import com.netifera.platform.api.model.layers.ILayerProvider;
import com.netifera.platform.model.TreeStructureContext;
import com.netifera.platform.ui.internal.spaces.Activator;
import com.netifera.platform.ui.updater.StructuredViewerUpdater;

public class SpaceTreeUpdater {
	private final TreeBuilder treeBuilder;
//	private final StructuredViewer viewer;
	private final StructuredViewerUpdater updater;
	private final IEventHandler spaceListener;
	private final ISpace space;

	SpaceTreeUpdater(ISpace space, final StructuredViewer treeViewer) {
		if(space == null || treeViewer == null) {
			throw new IllegalArgumentException("space=" + space + ", viewer=" + treeViewer);
		}
		this.space = space;
//		this.viewer = treeViewer;
		this.updater = StructuredViewerUpdater.get(treeViewer);
		List<ILayerProvider> layerProviders = new ArrayList<ILayerProvider>();
		for (ILayerProvider layerProvider: Activator.getDefault().getModel().getLayerProviders())
			if (layerProvider.isDefaultEnabled())
				layerProviders.add(layerProvider);
		this.treeBuilder = new TreeBuilder(layerProviders);
		this.treeBuilder.setListener(createUpdateListener());
		this.treeBuilder.setRoot(space.getRootEntity());
		
		this.spaceListener = createSpaceListener();
		this.space.addChangeListenerAndPopulate(spaceListener);
	}
	
	public void dispose() {
		space.removeChangeListener(spaceListener);
	}
	
	public IShadowEntity getRootEntity() {
		return treeBuilder.getRoot();
	}
	
	public TreeBuilder getTreeBuilder() {
		return treeBuilder;
	}
	
	public ISpace getSpace() {
		return space;
	}
	
	private IEventHandler createSpaceListener() {
		return new IEventHandler() {
			public void handleEvent(final IEvent event) {
				if(event instanceof ISpaceContentChangeEvent) {
					handleSpaceChange((ISpaceContentChangeEvent)event);
				}
			}
		};
	}
	
	private void handleSpaceChange(ISpaceContentChangeEvent event) {
		if(event.isCreationEvent()) {
			treeBuilder.addEntity(event.getEntity());
		} else if(event.isUpdateEvent()) {
			if(treeBuilderHasValidRoot())
				treeBuilder.updateEntity(event.getEntity());
		} else if(event.isRemovalEvent()) {
			treeBuilder.removeEntity(event.getEntity());
		}
	}

	private ITreeBuilderListener createUpdateListener() {
		return new ITreeBuilderListener() {

			public void entityAdded(IShadowEntity entity, IShadowEntity parent) {
//				if(treeViewer.getControl().isDisposed())
//					return;
				if (parent == treeBuilder.getRoot())
					updater.refresh();
				updater.refresh(parent);
			}

			public void entityChanged(IShadowEntity entity) {
//				if(treeViewer.getControl().isDisposed())
//					return;
				updater.update(entity,null);
			}

			public void entityRemoved(IShadowEntity entity, IShadowEntity parent) {
//				if(treeViewer.getControl().isDisposed())
//					return;
				if (parent == treeBuilder.getRoot())
					updater.refresh();
				updater.refresh(parent);
			}
		};
	}
	
	private boolean treeBuilderHasValidRoot() {
		return (treeBuilder.getRoot() != null && (treeBuilder.getRoot().getStructureContext() instanceof TreeStructureContext));
	}
	
	public List<ILayerProvider> getLayers() {
		return treeBuilder.getLayers();
	}
	
	public void addLayer(ILayerProvider layerProvider) {
		treeBuilder.addLayer(layerProvider);
		layersChanged();
	}
	
	public void removeLayer(ILayerProvider layerProvider) {
		treeBuilder.removeLayer(layerProvider);
		layersChanged();
	}
	
	private void layersChanged() {
		treeBuilder.setRoot(space.getRootEntity());
		for (IEntity entity: space.getEntities()) {
			treeBuilder.addEntity(entity);
		}
		updater.refresh();
	}
}
