package com.netifera.platform.ui.graphs.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.ui.graphs.GraphControl;

public class ToggleAnimationAction extends Action {
	
	private static final String PLUGIN_NAME = "com.netifera.platform.ui.graphs";
	
	final private GraphControl control;
	final private ImageDescriptor pauseImage;
	final private ImageDescriptor resumeImage;
	
	public ToggleAnimationAction(GraphControl control) {
		this.control = control;
		pauseImage = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_NAME, "icons/pause.png");
		resumeImage = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_NAME, "icons/play.png");
		update();
	}
	
	public void run() {
		control.setAnimationEnabled(!control.isAnimationEnabled());
		update();
	}
	
	private void update() {
		if(control.isAnimationEnabled()) {
			setImageDescriptor(pauseImage);
			setToolTipText("Pause Animation");
		} else {
			setImageDescriptor(resumeImage);
			setToolTipText("Resume Animation");
		}
	}
}
