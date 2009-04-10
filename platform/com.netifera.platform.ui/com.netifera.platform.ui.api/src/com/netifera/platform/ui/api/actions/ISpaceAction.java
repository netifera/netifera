package com.netifera.platform.ui.api.actions;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.tools.IToolConfiguration;

public interface ISpaceAction extends IAction {
	void setSpace(ISpace space);
	ISpace getSpace();
	
	IToolConfiguration getConfiguration();
}
