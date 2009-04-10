package com.netifera.platform.ui.spaces.visualizations;

import java.util.Set;

import com.netifera.platform.api.model.ISpace;

public interface ISpaceVisualizationFactory {
	Set<String> getVisualizationNames();
	ISpaceVisualization create(String name, ISpace space);
}
