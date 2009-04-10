package com.netifera.platform.ui.world.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.ui.world.WorldView;

public class TogglePlaceNamesAction extends Action {
	
	private static final String PLUGIN_NAME = "com.netifera.platform.ui.world";

	final private WorldView view;
	
	public TogglePlaceNamesAction(WorldView view) {
		this.view = view;
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_NAME, "icons/placenames.png"));
		update();
	}
	
	public void run() {
		view.setPlaceNamesEnabled(!view.isPlaceNamesEnabled());
		update();
	}
	
	private void update() {
		if(view.isPlaceNamesEnabled()) {
			setChecked(true);
			setToolTipText("Hide Place Names");
		} else {
			setChecked(false);
			setToolTipText("Show Place Names");
		}
	}
}
