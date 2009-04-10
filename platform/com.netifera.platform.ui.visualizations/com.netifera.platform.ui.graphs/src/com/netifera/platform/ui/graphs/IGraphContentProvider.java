package com.netifera.platform.ui.graphs;

import org.eclipse.jface.viewers.IContentProvider;

import prefuse.data.Graph;

public interface IGraphContentProvider extends IContentProvider {
	Graph getGraph();
}
