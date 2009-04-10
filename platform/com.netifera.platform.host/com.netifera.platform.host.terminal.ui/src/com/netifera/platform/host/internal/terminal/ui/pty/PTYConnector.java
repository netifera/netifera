package com.netifera.platform.host.internal.terminal.ui.pty;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.host.internal.terminal.ui.Activator;
import com.netifera.platform.host.terminal.ITerminal;
import com.netifera.platform.host.terminal.ITerminalManager;
import com.netifera.platform.host.terminal.ITerminalOutputHandler;

public class PTYConnector extends TerminalConnectorImpl {

	private OutputStream outputStream;
	private IProbe probe;
	private ITerminal terminalInstance;
	
	PTYConnector(IProbe probe) {
		this.probe = probe;
	}
	
	public void connect(final ITerminalControl control) {
		super.connect(control);

		control.setState(TerminalState.CONNECTING);
		ITerminalManager terminalManager = Activator.getInstance().getTerminalManagerFactory().createForProbe(probe);
		
		terminalInstance = terminalManager.openTerminal("/bin/bash", new ITerminalOutputHandler() {

			public void terminalOutput(String ptyName, byte[] data, int length) {
				try {
					control.getRemoteToTerminalOutputStream().write(data, 0, data.length);
				} catch (IOException e) {
					
					e.printStackTrace();
				}	
			}

			public void terminalClosed(String ptyName) {
				control.displayTextInTerminal("\nTerminal Closed\n");
				disconnect();				
			}
			
		});
		if(terminalInstance == null) {
			control.setState(TerminalState.CLOSED);
			return;
		}
		outputStream = new PtyOutputStream(terminalInstance);
		control.setState(TerminalState.CONNECTED);
	}
	
	@Override
	public String getSettingsSummary() {
		return null;
	}

	@Override
	public OutputStream getTerminalToRemoteStream() {
		return outputStream;
	}
	
	public void setTerminalSize(int newWidth, int newHeight) {
		terminalInstance.setSize(newWidth, newHeight);
	}

}
