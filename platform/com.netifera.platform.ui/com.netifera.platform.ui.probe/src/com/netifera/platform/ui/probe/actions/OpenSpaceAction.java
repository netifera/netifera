package com.netifera.platform.ui.probe.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.ui.probe.Activator;
import com.netifera.platform.ui.probe.views.ProbeListView;
import com.netifera.platform.ui.spaces.SpaceEditorInput;

public class OpenSpaceAction extends Action {

	private ProbeListView view;
	
	public OpenSpaceAction(ProbeListView view) {
		this.view = view;
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/new_space.png"));
		setText("Open New Space");
	}

	public void run() {
		IProbe probe = view.getSelectedProbe();
		if (probe != null)
			openSpaceForProbe(probe);
	}
	
	private void openSpaceForProbe(IProbe probe) {
		final ISpace space = openSpace(probe);
		openEditor(space);
	}
	
	private ISpace openSpace(IProbe probe) {
		final IWorkspace workspace = Activator.getDefault().getModel().getCurrentWorkspace();
		final ISpace space = workspace.createSpace(probe.getEntity(), probe);
		space.open();
		return space;
	}
	
	private void openEditor(ISpace space) {
		final IEditorInput input = new SpaceEditorInput(space);
		try {
			view.getViewSite().getPage().openEditor(input, SpaceEditorInput.ID);
		} catch(PartInitException e) {
			// XXX
			e.printStackTrace();
		}
	}
}
