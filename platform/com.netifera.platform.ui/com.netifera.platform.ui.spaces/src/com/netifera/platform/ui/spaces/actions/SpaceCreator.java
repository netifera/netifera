package com.netifera.platform.ui.spaces.actions;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.ui.internal.spaces.Activator;
import com.netifera.platform.ui.spaces.SpaceEditorInput;
import com.netifera.platform.ui.spaces.editors.SpaceEditor;

public class SpaceCreator {
	private final IWorkbenchWindow window;
	
	public SpaceCreator(IWorkbenchWindow window) {
		this.window = window;
	}
	
	public void create() {
		final IProbe probe = getProbeForNewSpace();
		final ISpace space = openSpace(probe);
		openEditor(space);
	}

	public void create(String name) {
		final IProbe probe = getProbeForNewSpace();
		final ISpace space = openSpace(probe);
		space.setName(name);
		openEditor(space);
	}

	/*
	 * If there is an active space, copy the probe from that space.  Otherwise use local probe.
	 */
	private IProbe getProbeForNewSpace() {
		final IProbeManagerService probeManager = Activator.getDefault().getProbeManager();
		final IProbe probe = getProbeForActiveEditor(probeManager);
		if(probe == null) 
			return probeManager.getLocalProbe();
		else 
			return probe;
	}

	private IProbe getProbeForActiveEditor(IProbeManagerService probeManager) {
		final IEditorPart editor = window.getActivePage().getActiveEditor();
		if(editor == null) 
			return null;
		
		final IEditorInput input = editor.getEditorInput();
		if(!(input instanceof SpaceEditorInput)) 
			return null;
		
		final ISpace space = ((SpaceEditorInput)input).getSpace();
		return probeManager.getProbeById(space.getProbeId());
		
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
			window.getActivePage().openEditor(input, SpaceEditor.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
