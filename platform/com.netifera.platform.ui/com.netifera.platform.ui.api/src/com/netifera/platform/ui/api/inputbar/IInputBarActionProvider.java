package com.netifera.platform.ui.api.inputbar;

import java.util.List;

import org.eclipse.jface.action.IAction;

public interface IInputBarActionProvider {
	List<IAction> getActions(long realm, long space, String input);
}
