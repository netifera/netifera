package com.netifera.platform.ui.internal.graphs;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.ui.spaces.visualizations.ISpaceVisualization;
import com.netifera.platform.ui.spaces.visualizations.ISpaceVisualizationProvider;

public class SpaceGraphVisualizationProvider implements ISpaceVisualizationProvider {

	public String getName() {
		return "Graph";
	}

	public ISpaceVisualization create(ISpace space) {
		return new SpaceGraphVisualization(space);
	}
}
