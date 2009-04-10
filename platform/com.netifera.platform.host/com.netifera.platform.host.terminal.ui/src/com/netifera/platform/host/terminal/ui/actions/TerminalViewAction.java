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

import org.eclipse.jface.action.Action;

import com.netifera.platform.host.terminal.ui.view.ITerminalView;

abstract public class TerminalViewAction extends Action {
    protected final ITerminalView fTarget;
    public TerminalViewAction(ITerminalView target, String strId) {
		this(target,strId,0);
	}
    public TerminalViewAction(ITerminalView target, String strId, int style) {
		super("",style); //$NON-NLS-1$

		fTarget = target;

		setId(strId);
	}
	abstract public void run();
}
