/*******************************************************************************
 * Copyright (c) 2004, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins 
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package com.netifera.platform.host.terminal.ui.actions;

import com.netifera.platform.host.internal.terminal.ui.Activator;
import com.netifera.platform.host.terminal.ui.view.ITerminalView;

public class RunAction extends TerminalViewAction
{
    public RunAction(ITerminalView target)
    {
        super(target,
              RunAction.class.getName());

        setText("Run Terminal Action..");
		setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/run.png"));
    }
	public void run() {
		fTarget.onTerminalRunAction();
	}
}
