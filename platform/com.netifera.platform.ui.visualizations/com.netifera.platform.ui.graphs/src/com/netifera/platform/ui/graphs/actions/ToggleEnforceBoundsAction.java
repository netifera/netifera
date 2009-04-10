package com.netifera.platform.ui.graphs.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.ui.graphs.GraphControl;

public class ToggleEnforceBoundsAction extends Action {
	
	private static final String PLUGIN_NAME = "com.netifera.platform.ui.graphs";
	
	final private GraphControl control;
	
	public ToggleEnforceBoundsAction(GraphControl control) {
		this.control = control;
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_NAME, "icons/enforce-bounds.png"));
		update();
	}
	
	public void run() {
		control.setEnforceBounds(!control.isEnforceBounds());
		update();
	}
	
	private void update() {
		if(control.isEnforceBounds()) {
			setChecked(true);
			setToolTipText("Don't Enforce Bounds");
		} else {
			setChecked(false);
			setToolTipText("Enforce Bounds");
		}
	}
}
