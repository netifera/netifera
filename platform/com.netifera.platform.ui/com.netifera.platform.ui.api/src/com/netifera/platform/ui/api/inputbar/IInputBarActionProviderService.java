package com.netifera.platform.ui.api.inputbar;

import java.util.List;

import org.eclipse.jface.action.IAction;

public interface IInputBarActionProviderService {
	List<IAction> getActions(long realm, long space, String input);
}
