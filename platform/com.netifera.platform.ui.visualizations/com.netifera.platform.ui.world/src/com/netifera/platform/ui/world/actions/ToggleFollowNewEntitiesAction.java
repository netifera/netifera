package com.netifera.platform.ui.world.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.ui.world.WorldView;

public class ToggleFollowNewEntitiesAction extends Action {
	
	private static final String PLUGIN_NAME = "com.netifera.platform.ui.world";

	final private WorldView view;
	
	public ToggleFollowNewEntitiesAction(WorldView view) {
		this.view = view;
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_NAME, "icons/follow_new.png"));
		update();
	}
	
	public void run() {
		view.setFollowNewEnabled(!view.isFollowNewEnabled());
		update();
	}
	
	private void update() {
		if(view.isFollowNewEnabled()) {
			setChecked(true);
			setToolTipText("Don't Fly To New Entities");
		} else {
			setChecked(false);
			setToolTipText("Fly To New Entities");
		}
	}
}
