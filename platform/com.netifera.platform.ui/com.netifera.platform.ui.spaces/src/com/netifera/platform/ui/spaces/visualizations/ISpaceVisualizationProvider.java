package com.netifera.platform.ui.spaces.visualizations;

import com.netifera.platform.api.model.ISpace;

public interface ISpaceVisualizationProvider {
	String getName();
	ISpaceVisualization create(ISpace space);
}
