package com.netifera.platform.ui.world.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.ui.internal.world.Activator;
import com.netifera.platform.ui.world.WorldView;

public class ToggleLabelsAction extends Action {
	
	final private WorldView view;
	
	public ToggleLabelsAction(WorldView view) {
		this.view = view;
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/labels.png"));
		update();
	}
	
	public void run() {
		view.setLabelsEnabled(!view.isLabelsEnabled());
		update();
	}
	
	private void update() {
		if(view.isLabelsEnabled()) {
			setChecked(true);
			setToolTipText("Hide Labels");
		} else {
			setChecked(false);
			setToolTipText("Show Labels");
		}
	}
}
