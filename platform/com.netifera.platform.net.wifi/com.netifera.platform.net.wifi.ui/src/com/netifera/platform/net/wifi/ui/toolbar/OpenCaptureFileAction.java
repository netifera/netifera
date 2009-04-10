package com.netifera.platform.net.wifi.ui.toolbar;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.net.wifi.ui.Activator;

public class OpenCaptureFileAction extends Action {
	public final static String ID = "wifi-capture-file-action";
	private WifiToolbar toolbar;
	
	public OpenCaptureFileAction(WifiToolbar toolbar) {
		setId(ID);
		this.toolbar = toolbar;
		setToolTipText("Open Capture File");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/open_capfile.png"));
	}


}
