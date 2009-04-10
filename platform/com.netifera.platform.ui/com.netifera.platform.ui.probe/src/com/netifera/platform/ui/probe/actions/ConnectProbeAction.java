package com.netifera.platform.ui.probe.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.ui.probe.Activator;

public class ConnectProbeAction extends Action {
	private final StructuredViewer viewer;
	public ConnectProbeAction(StructuredViewer viewer) {
		this.viewer = viewer;
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/connect.png"));
		setText("Connect to Probe");
	}
	
	@Override
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		Object element = selection.getFirstElement();
		if(!(element instanceof IProbe)) {
			return;
		}
		IProbe probe = (IProbe) element;
		probe.connect();	
	}

}
