package com.netifera.platform.ui.internal.spaces.visualizations;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.ui.spaces.visualizations.ISpaceVisualization;
import com.netifera.platform.ui.spaces.visualizations.ISpaceVisualizationProvider;

public class SpaceTreeVisualizationProvider implements ISpaceVisualizationProvider {
	
	public String getName() {
		return "Tree";
	}

	public ISpaceVisualization create(ISpace space) {
		return new SpaceTreeVisualization(space);
	}
}
