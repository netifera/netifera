package com.netifera.platform.ui.spaces.visualizations;

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.swt.widgets.Composite;

import com.netifera.platform.api.model.IEntity;

public interface ISpaceVisualization {
	ContentViewer createViewer(Composite parent);
	void addContributions(IContributionManager contributions);
	void focusEntity(IEntity entity);
//	void dispose();
}
