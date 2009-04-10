package com.netifera.platform.host.terminal.ui;

import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.model.IEntity;

public interface ITerminalActionProvider {
	
	/* 
	 * Actions available to be performed on the given host via a terminal connection
	 * 
	 * Returns a list of ISpaceActions or ITerminalActions
	 * (the caller is responsible of setting the ISpace)
	 */
	List<IAction> getActions(long realm, long space, IEntity hostEntity);
}
