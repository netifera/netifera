package com.netifera.platform.ui.probe;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.ui.IEditorPart;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.ui.probe.status.ProbeStatusLine;
import com.netifera.platform.ui.spaces.IStatusContribution;
import com.netifera.platform.ui.spaces.SpaceEditorInput;

public class ProbeStatusContribution implements IStatusContribution {

	private final ProbeStatusLine statusLine = new ProbeStatusLine();
	public ControlContribution getContribution() {
		return statusLine;
	}

	public void setActiveEditor(IEditorPart editor) {
		if(editor.getEditorInput() instanceof SpaceEditorInput) {
			SpaceEditorInput spaceEditorInput = (SpaceEditorInput) editor.getEditorInput();
			IProbe probe = spaceEditorInput.getProbeForSpace();
			statusLine.setProbe(probe);
		}
		
	}

}
