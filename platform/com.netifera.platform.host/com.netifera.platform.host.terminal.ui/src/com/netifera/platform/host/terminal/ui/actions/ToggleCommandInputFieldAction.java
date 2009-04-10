/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial implementation
  *******************************************************************************/
package com.netifera.platform.host.terminal.ui.actions;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.host.internal.terminal.ui.Activator;
import com.netifera.platform.host.terminal.ui.view.ITerminalView;

public class ToggleCommandInputFieldAction extends TerminalViewAction
{
    public ToggleCommandInputFieldAction(ITerminalView target) {
        super(target,
              ToggleCommandInputFieldAction.class.getName(),IAction.AS_CHECK_BOX);

        setText("Command Input Field");
		setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/command_input_field.png"));
		setDisabledImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/command_input_field_disabled.png"));
        setChecked(fTarget.hasCommandInputField());
    }

    public void run() {
		fTarget.setCommandInputField(!fTarget.hasCommandInputField());
		setChecked(fTarget.hasCommandInputField());
	}
}
