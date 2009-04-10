package com.netifera.platform.ui.world.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.ui.internal.world.Activator;
import com.netifera.platform.ui.world.WorldView;

public class ToggleOverviewAction extends Action {
	
	final private WorldView view;
	
	public ToggleOverviewAction(WorldView view) {
		this.view = view;
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/overview.png"));
		update();
	}
	
	public void run() {
		view.setOverviewEnabled(!view.isOverviewEnabled());
		update();
	}
	
	private void update() {
		if(view.isOverviewEnabled()) {
			setChecked(true);
			setToolTipText("Hide Overview");
		} else {
			setChecked(false);
			setToolTipText("Show Ovewview");
		}
	}
}
