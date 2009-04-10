package com.netifera.platform.ui.spaces.tree;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.IStructureContext;
import com.netifera.platform.api.model.layers.ILayerProvider;
import com.netifera.platform.model.TreeStructureContext;

public class SpaceTreeContentProvider implements ITreeContentProvider {
	private static final boolean DEBUG_CONTENT_PROVIDER = false;

	private SpaceTreeUpdater treeUpdater;
	
	public Object[] getChildren(Object node) {
		if(!(node instanceof IShadowEntity)) {
			throw new IllegalArgumentException();
		}
		final List<IShadowEntity> children = getChildEntities((IShadowEntity) node);
		
		if(DEBUG_CONTENT_PROVIDER) {
			debug("getChildren(" + node + ") --> " + children.toArray());
		}
		return children.toArray();
	}
	
	public TreeBuilder getTreeBuilder() {
		return treeUpdater.getTreeBuilder();
	}
	
	private List<IShadowEntity> getChildEntities(IShadowEntity entity) {
		TreeStructureContext tsc = nodeToTSC(entity);
		if(tsc.hasChildren()) {
			List<IShadowEntity> children = tsc.getChildren();

			return children;
		} else {
			return Collections.emptyList();
		}
	}
	
	public Object getParent(Object node) {
		if(DEBUG_CONTENT_PROVIDER) {
			debug("getParent(" + node + ") --> " + nodeToTSC(node).getParent());
		}
		return nodeToTSC(node).getParent();
	}

	public boolean hasChildren(Object node) {
		if(DEBUG_CONTENT_PROVIDER) {
			debug("hasChildren(" + node + ") --> " + nodeToTSC(node).hasChildren());
		}
		return nodeToTSC(node).hasChildren();
	}

	public Object[] getElements(Object input) {
		if(input != treeUpdater.getSpace()) {
			throw new IllegalArgumentException();
		}
		if(DEBUG_CONTENT_PROVIDER) {
			debug("getElements(" + input + ") --> " + getChildEntities(treeUpdater.getRootEntity()));
		}
		
		return getChildEntities(treeUpdater.getRootEntity()).toArray();
	}
	
	public void dispose() {
		if(treeUpdater != null) {
			treeUpdater.dispose();
		}
		treeUpdater = null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(!validInputChange(viewer, newInput)) {
			return;
		}
		
		if(DEBUG_CONTENT_PROVIDER) {
			debug("inputChanged old = " + oldInput + " new = " + newInput);
		}
		
		if(treeUpdater != null) {
			treeUpdater.dispose();
		}
		
		treeUpdater = new SpaceTreeUpdater((ISpace)newInput, (StructuredViewer) viewer);
	}
	
	private void debug(String message) {
		System.err.println("SpaceTreeContentProvider : " + message);
	}
	
	private boolean validInputChange(Viewer viewer, Object newInput) {
		return (viewer instanceof StructuredViewer) && (newInput instanceof ISpace);
	}
	
	/*
	 * Convert a tree node to the corresponding TreeStructureContext
	 */
	private TreeStructureContext nodeToTSC(Object node) {
		if(node instanceof IShadowEntity) {
			IStructureContext sc = ((IShadowEntity)node).getStructureContext();
			
			if(sc instanceof TreeStructureContext) {
				return (TreeStructureContext) sc;
			}
						
		}
		
		throw new IllegalStateException("Could not convert node to TreeStructureContext");		
	}
	
	public List<ILayerProvider> getLayers() {
		return treeUpdater.getLayers();
	}
	
	public void addLayer(ILayerProvider layerProvider) {
		treeUpdater.addLayer(layerProvider);
	}
	
	public void removeLayer(ILayerProvider layerProvider) {
		treeUpdater.removeLayer(layerProvider);
	}
}
