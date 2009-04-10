package com.netifera.platform.ui.internal.spaces.visualizations;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.ui.spaces.visualizations.ISpaceVisualization;
import com.netifera.platform.ui.spaces.visualizations.ISpaceVisualizationProvider;

public class SpaceTableVisualizationProvider implements ISpaceVisualizationProvider {

	public String getName() {
		return "Table";
	}

	public ISpaceVisualization create(ISpace space) {
		return new SpaceTableVisualization(space);
	}
}
