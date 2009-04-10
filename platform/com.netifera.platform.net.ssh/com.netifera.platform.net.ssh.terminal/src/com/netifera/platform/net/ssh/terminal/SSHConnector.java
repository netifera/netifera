/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [225792] Rename SshConnector.getTelnetSettings() to getSshSettings()
 * Martin Oberhuber (Wind River) - [225853][api] Provide more default functionality in TerminalConnectorImpl 
 *******************************************************************************/
package com.netifera.platform.net.ssh.terminal;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;

import com.trilead.ssh2.Session;

public class SSHConnector extends TerminalConnectorImpl {
	private OutputStream outputStream;
	private InputStream inputStream;
	private SSHConnection connection;
	private final SSHSettings settings;
	private Session session;
	private int width;
	private int height;
	
	public SSHConnector() {
		this(new SSHSettings());
	}
	public SSHConnector(SSHSettings settings) {
		this.settings = settings;
	}
	public void initialize() throws Exception {
//		fJsch=new JSch();
	}
	public void connect(ITerminalControl control) {
		super.connect(control);
		connection = new SSHConnection(this,control);
		connection.start();
	}
	synchronized public void doDisconnect() {
		connection.disconnect();
		if (getInputStream() != null) {
			try {
				getInputStream().close();
			} catch (Exception exception) {
				Logger.logException(exception);
			}
		}

		if (getTerminalToRemoteStream() != null) {
			try {
				getTerminalToRemoteStream().close();
			} catch (Exception exception) {
				Logger.logException(exception);
			}
		}
	}
	public void setTerminalSize(int newWidth, int newHeight) {
		if(session!=null && (newWidth!=width || newHeight!=height)) {
			//avoid excessive communications due to change size requests by caching previous size
//			session.setPtySize(newWidth, newHeight, 8*newWidth, 8*newHeight);
			width=newWidth;
			height=newHeight;
		}
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	public OutputStream getTerminalToRemoteStream() {
		return outputStream;
	}
	void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	/**
	 * Return the SSH Settings.
	 *
	 * @return the settings for a concrete connection.
	 * @since org.eclipse.tm.terminal.ssh 2.0 renamed from getTelnetSettings()
	 */
	public ISSHSettings getSettings() {
		return settings;
	}
	public ISettingsPage makeSettingsPage() {
		return new SSHSettingsPage(settings);
	}
	public String getSettingsSummary() {
		return settings.getSummary();
	}
	public void load(ISettingsStore store) {
		settings.load(store);
	}
	public void save(ISettingsStore store) {
		settings.save(store);
	}
	
	Session getSession() {
		return session;
	}
	
	void setSession(Session session) {
		this.session = session;
		width=-1;
		height=-1;
	}
}
