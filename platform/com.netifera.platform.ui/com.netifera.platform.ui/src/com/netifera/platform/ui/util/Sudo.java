package com.netifera.platform.ui.util;


import java.io.File;
import java.io.IOException;

import org.eclipse.swt.widgets.Display;

import com.netifera.platform.ui.dialogs.AskPasswordDialog;

public class Sudo {

	private static final String[] pathsGUI = { "/usr/bin/gksudo",
		"/usr/bin/kdesu", "/usr/bin/kdesudo" };
	private static final String[] pathsSudo = { "/usr/bin/sudo" };
	private final String guiPath;
	private final String sudoPath;
	private String prompt;
	private String password;

	public Sudo() {
		guiPath = getAvailable(pathsGUI);
		sudoPath = getAvailable(pathsSudo);
	}

	public boolean canExecute() {
		return (guiPath != null || (sudoPath != null && (password != null || canAskPassword())));
	}

	public boolean execute(String command) {

		if (command == null || command.length() == 0) {
			return false;
		}

		return execute(new String[] { command });
	}

	public boolean system(String command) {
		if(guiPath == null) {
			return execute(new String[]{"/bin/sh","-c",command});
		}
		else {
			return execute( "/bin/sh -c '" + command + "'");
		}
	}
	
	public boolean execute(String[] cmdarray) {

		if (cmdarray == null || cmdarray.length == 0 || !canExecute()) {
			return false;
		}

		String[] sudocmdarray = buildCommandArray(cmdarray);

		if (guiPath == null && password == null) {
			password = askPassword(prompt);
			if(password == null) {
				return false;
			}
		}

		try {
			Process proc = Runtime.getRuntime().exec(sudocmdarray);

			if (password != null) {
				password += "\n";
				byte[] pass = password.getBytes("US-ASCII");
				proc.getOutputStream().write(pass);
				proc.getOutputStream().flush();
			}

		} catch (IOException e) {
			return false;
		}
		return true;

	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	/* Override canAskPassword and askPassword */
	protected boolean canAskPassword() {
		return true;
	}

	protected String askPassword(String prompt) {
		final AskPasswordDialog askDialog = new AskPasswordDialog(Display.getDefault().getActiveShell());
		Display.getDefault().syncExec( new Runnable() {

			public void run() {
				if (askDialog.open() == AskPasswordDialog.OK) {
					password = askDialog.getPassword();
				} else {
					password = null;
				}
			}

		});

		return password;
	}

	private String[] buildCommandArray(String[] cmdarray) {

		if (guiPath != null) {
			String[] newarray = new String[cmdarray.length + 1];
			newarray[0] = guiPath;
			System.arraycopy(cmdarray, 0, newarray, 1, cmdarray.length);
			return newarray;
		} else {
			String[] sudoArgs = { "-S", "-u", "root", "--" };
			String[] newarray = new String[cmdarray.length + sudoArgs.length + 1];
			newarray[0] = sudoPath;
			System.arraycopy(sudoArgs, 0, newarray, 1, sudoArgs.length);
			System.arraycopy(cmdarray, 0, newarray, sudoArgs.length + 1,
					cmdarray.length);
			return newarray;
		}
	}

	private String getAvailable(String paths[]) {
		for (int i = 0; i < paths.length; i++) {
			File file = new File(paths[i]);
			if (file.exists()) {
				return paths[i];
			}
		}
		return null;
	}
}
