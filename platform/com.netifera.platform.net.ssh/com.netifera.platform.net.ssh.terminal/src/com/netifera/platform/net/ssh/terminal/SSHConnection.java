package com.netifera.platform.net.ssh.terminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;

class SSHConnection extends Thread {
	private static int number; //FIXME should be atomic
	private final ITerminalControl control;
	private final SSHConnector connector;
	private Connection client;
	private boolean disconnectHasBeenCalled;
	
	protected SSHConnection(SSHConnector connector, ITerminalControl control) {
		super("SSHConnection-"+number++);
		this.control = control;
		this.connector = connector;
		control.setState(TerminalState.CONNECTING);
	}

	static void shutdown() {
		//TODO: Store all sessions in a pool and disconnect them on shutdown
	}

	//----------------------------------------------------------------------
	// </copied code from org.eclipse.team.cvs.ssh2/JSchSession (Copyright IBM)>
	//----------------------------------------------------------------------

	public void run() {
		boolean connectSucceeded = false;
		String host = "";
		int port = 22;
		try {
			int nTimeout = connector.getSettings().getTimeout() * 1000;
			int nKeepalive = connector.getSettings().getKeepalive() * 1000;
			host = connector.getSettings().getHost();
			String user = connector.getSettings().getUser();
			String password = connector.getSettings().getPassword();
			port = connector.getSettings().getPort();

            // dont try to connect if disconnect has been requested already
			synchronized (this) {
				if (disconnectHasBeenCalled)
					return;
			}
			
			Connection client = new Connection(host, port);
			client.connect();
			client.authenticateWithPassword(user, password);

			synchronized (this) {
				this.client = client;
			}

	        Session session = client.openSession();
	        session.requestPTY("ansi", 0, 0, 0, 0, null);
	        session.startShell();

			connectSucceeded = true;
			connector.setInputStream(session.getStdout());
			connector.setOutputStream(session.getStdin());
			connector.setSession(session);
			control.setState(TerminalState.CONNECTED);
			try {
				// read data until the connection gets terminated
				// TODO include stderr
				readDataForever(connector.getInputStream());
			} catch (InterruptedIOException e) {
				// we got interrupted: we are done...
			}
		} catch (Exception e) {
			Throwable cause = e;
			while (cause.getCause() != null) {
				cause = cause.getCause();
			}
			String msg = cause.getMessage();
			if (!connectSucceeded) {
				String hostPort = host;
				if (port != 22) {
					hostPort = hostPort + ':' + port;
				}
				msg = "Error connecting to "+hostPort+": "+ msg;
			}
			connectFailed(msg, msg);
		} finally {
			// make sure the terminal is disconnected when the thread ends
			try {
				disconnect();
			} finally {
				// when reading is done, we set the state to closed
				control.setState(TerminalState.CLOSED);
			}
		}
	}

	private synchronized boolean isConnected() {
		return !disconnectHasBeenCalled && client != null;
	}

	/**
	 * disconnect the ssh session
	 */
	void disconnect() {
		interrupt();
		synchronized (this) {
			disconnectHasBeenCalled=true;
			if(client!=null) {
				client.close();
			}
		}
	}
	
	/**
	 * Read the data from the ssh connection and display it in the terminal.
	 * @param in
	 * @throws IOException
	 */
	private void readDataForever(InputStream in) throws IOException {
		// read the data
		byte bytes[]=new byte[32*1024];
		int n;
		// read until the thread gets interrupted....
		while( (n=in.read(bytes))!=-1) {
			control.getRemoteToTerminalOutputStream().write(bytes,0,n);
		}
	}

	protected static Display getStandardDisplay() {
    	Display display = Display.getCurrent();
    	if( display==null ) {
    		display = Display.getDefault();
    	}
    	return display;
    }

    private void connectFailed(String terminalText, String msg) {
		Logger.log(terminalText);
		control.displayTextInTerminal(terminalText);
		// control.setMsg(msg);
	}
}