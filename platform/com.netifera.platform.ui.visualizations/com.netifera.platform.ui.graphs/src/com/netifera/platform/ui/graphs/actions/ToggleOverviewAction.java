package com.netifera.platform.ui.graphs.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.ui.graphs.GraphControl;

public class ToggleOverviewAction extends Action {
	
	private static final String PLUGIN_NAME = "com.netifera.platform.ui.graphs";
	
	final private GraphControl control;
	
	public ToggleOverviewAction(GraphControl control) {
		this.control = control;
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_NAME, "icons/overview.png"));
		update();
	}
	
	public void run() {
		control.setOverviewEnabled(!control.isOverviewEnabled());
		update();
	}
	
	private void update() {
		if(control.isOverviewEnabled()) {
			setChecked(true);
			setToolTipText("Hide Overview");
		} else {
			setChecked(false);
			setToolTipText("Show Ovewview");
		}
	}
}
