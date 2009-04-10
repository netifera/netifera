package com.netifera.platform.ui.spaces.editors;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;

import com.netifera.platform.ui.internal.spaces.Activator;
import com.netifera.platform.ui.spaces.IStatusContribution;

public class SpaceEditorActionBarContributor extends EditorActionBarContributor {
	
	public void contributeToStatusLine(IStatusLineManager statusLine) {
		IStatusContribution statusContribution = Activator.getDefault().getStatusContribution();
		if(statusContribution == null)
			return;
		
		statusLine.add(statusContribution.getContribution());
		
	}
	
	public void setActiveEditor(IEditorPart editor) {
		IStatusContribution statusContribution = Activator.getDefault().getStatusContribution();
		if(statusContribution == null)
			return;
		statusContribution.setActiveEditor(editor);
		
	}
}
