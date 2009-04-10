/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package com.netifera.platform.host.terminal.ui.actions;

import com.netifera.platform.host.internal.terminal.ui.Activator;
import com.netifera.platform.host.terminal.ui.view.ITerminalViewConnectionManager;
import com.netifera.platform.host.terminal.ui.view.ITerminalViewConnectionManager.ITerminalViewConnectionListener;

public class RemoveTerminalAction extends TerminalViewAction implements ITerminalViewConnectionListener 
{
	private final ITerminalViewConnectionManager fConnectionManager;
    public RemoveTerminalAction(ITerminalViewConnectionManager target)
    {
        super(null,
              RemoveTerminalAction.class.getName());
        fConnectionManager=target;
        
        setText("Close Terminal");
		setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/remove.png"));
		setDisabledImageDescriptor(Activator.getInstance().getImageRegistry().getDescriptor("icons/remove_disabled.png"));
        
        fConnectionManager.addListener(this);
		connectionsChanged();
    }
	public void run() {
		fConnectionManager.removeActive();
	}
	public void connectionsChanged() {
		setEnabled(fConnectionManager.size()>1);
	}
}
