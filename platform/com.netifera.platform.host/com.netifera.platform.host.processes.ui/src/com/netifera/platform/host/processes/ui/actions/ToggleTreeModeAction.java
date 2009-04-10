package com.netifera.platform.host.processes.ui.actions;

import org.eclipse.jface.action.Action;

import com.netifera.platform.host.internal.processes.ui.Activator;
import com.netifera.platform.host.processes.ui.ProcessListView;

public class ToggleTreeModeAction extends Action {
	final private ProcessListView view;
	
	public ToggleTreeModeAction(ProcessListView view) {
		this.view = view;
		setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/tree_mode.png"));
		update();
	}
	
	public void run() {
		view.setTreeMode(!view.isTreeMode());
		update();
	}
	
	private void update() {
		if(view.isTreeMode()) {
			setChecked(true);
			setToolTipText("Show as Table");
		} else {
			setChecked(false);
			setToolTipText("Show as Tree");
		}
	}

}
