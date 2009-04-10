package com.netifera.platform.ui.graphs.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.ui.graphs.GraphControl;

public class ToggleLabelsAction extends Action {
	
	private static final String PLUGIN_NAME = "com.netifera.platform.ui.graphs";
	
	final private GraphControl control;
	
	public ToggleLabelsAction(GraphControl control) {
		this.control = control;
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_NAME, "icons/labels.png"));
		update();
	}
	
	public void run() {
		control.setLabelsEnabled(!control.isLabelsEnabled());
		update();
	}
	
	private void update() {
		if(control.isLabelsEnabled()) {
			setChecked(true);
			setToolTipText("Hide Labels");
		} else {
			setChecked(false);
			setToolTipText("Show Labels");
		}
	}
}
