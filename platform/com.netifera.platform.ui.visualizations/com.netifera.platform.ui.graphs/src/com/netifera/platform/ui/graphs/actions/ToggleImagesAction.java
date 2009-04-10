package com.netifera.platform.ui.graphs.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.ui.graphs.GraphControl;

public class ToggleImagesAction extends Action {
	
	private static final String PLUGIN_NAME = "com.netifera.platform.ui.graphs";
	
	final private GraphControl control;
	
	public ToggleImagesAction(GraphControl control) {
		this.control = control;
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_NAME, "icons/images.png"));
		update();
	}
	
	public void run() {
		control.setImagesEnabled(!control.isImagesEnabled());
		update();
	}
	
	private void update() {
		if(control.isImagesEnabled()) {
			setChecked(true);
			setToolTipText("Hide Images");
		} else {
			setChecked(false);
			setToolTipText("Show Images");
		}
	}
}
