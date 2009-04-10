/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Michael Scharf (Wind River) - [240097] Allow paste with the middle mouse button
 *******************************************************************************/
/**
 *
 */
package com.netifera.platform.host.terminal.ui.view;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.tm.internal.terminal.control.CommandInputFieldWithHistory;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

import com.netifera.platform.host.terminal.ui.view.internal.SettingStorePrefixDecorator;

/**
 * This class represents one connection. The connection might be
 * closed or open.
 *
 */
class TerminalViewConnection implements ITerminalViewConnection {
	private static final String STORE_SUMMARY = "Summary"; //$NON-NLS-1$
	private static final String STORE_PART_NAME = "PartName"; //$NON-NLS-1$
	private static final String STORE_CONNECTION_TYPE = "ConnectionType"; //$NON-NLS-1$
    private static final String STORE_HAS_COMMAND_INPUT_FIELD = "HasCommandInputField"; //$NON-NLS-1$
	private static final String STORE_COMMAND_INPUT_FIELD_HISTORY = "CommandInputFieldHistory"; //$NON-NLS-1$

	final private ITerminalViewControl terminalViewControl;
	
	private String title;
	private String summary;
	private String history;
	private CommandInputFieldWithHistory commandInputField;
	private String partName;
	
	public TerminalViewConnection(ITerminalViewControl ctl) {
		terminalViewControl = ctl;
		terminalViewControl.getControl().addMouseListener(new MouseAdapter(){
			public void mouseUp(MouseEvent e) {
				// paste when the middle button is clicked
				if(e.button==2) // middle button
					terminalViewControl.paste();
			}});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.actions.ITerminalViewConnection#getName()
	 */
	public String getFullSummary() {
		// if the title is set, then we return the title, else the summary
		if(title==null)
			return makeSummary();
		return title;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.view.ITerminalViewConnection#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
//HACK		return TerminalViewPlugin.getDefault().getImageRegistry().getDescriptor(ImageConsts.IMAGE_TERMINAL_VIEW);
	}

	public ITerminalViewControl getCtlTerminal() {
		return terminalViewControl;
	}
	
	private ISettingsStore getStore(ISettingsStore store,ITerminalConnector connector) {
		return new SettingStorePrefixDecorator(store,connector.getId()+"."); //$NON-NLS-1$
	}
	
	public void loadState(ISettingsStore store) {
		System.err.println("TerminalViewConnection.loadState");

		partName=store.get(STORE_PART_NAME);
		summary=store.get(STORE_SUMMARY);
		history=store.get(STORE_COMMAND_INPUT_FIELD_HISTORY);

		// load the state of the connection types
		ITerminalConnector[] connectors=terminalViewControl.getConnectors();
		String connectionType=store.get(STORE_CONNECTION_TYPE);
		for (int i = 0; i < connectors.length; i++) {
			connectors[i].load(getStore(store,connectors[i]));
			// if this is active connection type
			if(connectors[i].getId().equals(connectionType))
				terminalViewControl.setConnector(connectors[i]);
		}

		if("true".equals(store.get(STORE_HAS_COMMAND_INPUT_FIELD))) //$NON-NLS-1$
			setCommandInputField(true);
	}

	public void saveState(ISettingsStore store) {
		System.err.println("TerminalViewConnection.saveState");

		store.put(STORE_PART_NAME, partName);
		store.put(STORE_SUMMARY,summary);
		store.put(STORE_COMMAND_INPUT_FIELD_HISTORY, history);
		if(commandInputField!=null)
			store.put(STORE_COMMAND_INPUT_FIELD_HISTORY, commandInputField.getHistory());
		else
			store.put(STORE_COMMAND_INPUT_FIELD_HISTORY, history);
		store.put(STORE_HAS_COMMAND_INPUT_FIELD,hasCommandInputField()?"true":"false");   //$NON-NLS-1$//$NON-NLS-2$
		ITerminalConnector[] connectors=terminalViewControl.getConnectors();
		for (int i = 0; i < connectors.length; i++) {
			connectors[i].save(getStore(store,connectors[i]));
		}
		if(terminalViewControl.getTerminalConnector()!=null) {
			store.put(STORE_CONNECTION_TYPE,terminalViewControl.getTerminalConnector().getId());
		}
	}
	
	public boolean hasCommandInputField() {
		return commandInputField!=null;
	}
	
	public void setCommandInputField(boolean on) {
		// save the old history
		if(commandInputField!=null) {
			history= commandInputField.getHistory();
			commandInputField=null;
		}
		if(on) {
			// TODO make history size configurable
			commandInputField=new CommandInputFieldWithHistory(100);
			commandInputField.setHistory(history);
		}
		terminalViewControl.setCommandInputField(commandInputField);
	}

	public void setState(TerminalState state) {
		// update the title....
		title=null;
	}

	public void setTerminalTitle(String title) {
		// When parameter 'title' is not null, it is a String containing text to
		// display in the view's content description line.  This is used by class
		// TerminalText when it processes an ANSI OSC escape sequence that commands
		// the terminal to display text in its title bar.
		this.title=title;
	}

	private String getStateDisplayName(TerminalState state) {
		if(state==TerminalState.CONNECTED) {
			return "CONNECTED";
		} else if(state==TerminalState.CONNECTING) {
			return "CONNECTING..";
		} else if(state==TerminalState.OPENED) {
			return "OPENED";
		} else if(state==TerminalState.CLOSED) {
			return "CLOSED";
		} else {
			throw new IllegalStateException(state.toString());
		}
	}

	private String makeSummary() {
		String strTitle = "";
		if(terminalViewControl.getTerminalConnector()==null){
			strTitle="No Connection Selected";
		} else {
			// When parameter 'data' is null, we construct a descriptive string to
			// display in the content description line.
			String strConnected = getStateDisplayName(terminalViewControl.getState());
			String summary = getSettingsSummary();
			//TODO Title should use an NLS String and com.ibm.icu.MessageFormat
			//In order to make the logic of assembling, and the separators, better adapt to foreign languages
			if(summary.length()>0)
				this.summary=summary+" - ";  //$NON-NLS-1$
			String name=terminalViewControl.getTerminalConnector().getName();
			if(name.length()>0) {
				name+=": "; //$NON-NLS-1$
			}
			strTitle = name + "("+ summary + strConnected + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return strTitle;
	}
	/**
	 * @return the setting summary. If there is no connection, or the connection
	 * has not been initialized, use the last stored state.
	 */
	private String getSettingsSummary() {
		if(terminalViewControl.getTerminalConnector().isInitialized())
			summary=terminalViewControl.getSettingsSummary();
		if(summary==null)
			return ""; //$NON-NLS-1$
		return summary;
	}

	public void setSummary(String summary) {
		this.summary=summary;
	}

	public String getPartName() {
		return partName==null?"Terminal":partName;
	}

	public void setPartName(String name) {
		partName=name;

	}
}