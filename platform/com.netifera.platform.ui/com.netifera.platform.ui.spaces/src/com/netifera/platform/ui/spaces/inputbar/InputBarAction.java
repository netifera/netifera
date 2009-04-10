package com.netifera.platform.ui.spaces.inputbar;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.ui.internal.spaces.Activator;

public class InputBarAction extends Action implements IWorkbenchAction {

	private final InputBar inputBar;
	
	public InputBarAction(InputBar bar) {
		this.inputBar = bar;
		setEnabled(false);
		bar.setAction(this);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, getImagePath()));
	}

	public void dispose() {		
	}
	
	public String getImagePath() {
		final String os = System.getProperty("osgi.os");
		if(os != null && os.equals("macosx")) {
			return "icons/add24.png";
		} else {
			return "icons/add.png";
		}
	}
	public void run() {
		inputBar.runAction();
	}


}
