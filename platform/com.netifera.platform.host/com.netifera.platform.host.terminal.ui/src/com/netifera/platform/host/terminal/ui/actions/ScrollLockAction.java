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

import org.eclipse.jface.action.IAction;

import com.netifera.platform.host.internal.terminal.ui.Activator;
import com.netifera.platform.host.terminal.ui.view.ITerminalView;

public class ScrollLockAction extends TerminalViewAction
{
    public ScrollLockAction(ITerminalView target)
    {
        super(target,
              ScrollLockAction.class.getName(),IAction.AS_CHECK_BOX);

        setText("Scroll &Lock");
        setToolTipText("Scroll Lock");
		setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/lock.png"));
		setDisabledImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/lock_disabled.png"));
    }
	public void run() {
		fTarget.setScrollLock(!fTarget.isScrollLock());
		setChecked(fTarget.isScrollLock());
	}
}
