package com.netifera.platform.ui.spaces;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.ui.IEditorPart;

public interface IStatusContribution {
	ControlContribution getContribution();
	void setActiveEditor(IEditorPart editor);

}
