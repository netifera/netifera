package com.netifera.platform.ui.application;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.netifera.platform.ui.application.workspaces.WorkspaceChooser;
import com.netifera.platform.ui.application.workspaces.WorkspaceOpenException;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {


	public Object start(IApplicationContext context) throws Exception {
		final Display display = PlatformUI.createDisplay();
		
		try {
			if(!setupWorkspace(display)) {
				return IApplication.EXIT_OK;
			}
		} catch(WorkspaceOpenException e) {
			MessageDialog.openError(null, "Initialization Error", "Failed to create workspace: " + e.getMessage());
			return IApplication.EXIT_OK;
			//XXX if the default workspace cannot be open, give the option to open a new one here
		}
		
		try {
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART)
				return IApplication.EXIT_RESTART;
			else
				return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}
	}

	private boolean setupWorkspace(Display display) {
		final Location loc = Platform.getInstanceLocation();
		if(!loc.isSet()) {
			/* if the instance location is not set, set it using the Workspace chooser */
			if(!setLocation(loc))
				return false;
		}
	
		try {
			if(loc.isLocked()) {
				throw new WorkspaceOpenException("Another instance of Netifera is already using this workspace.");
			}
		} catch (IOException e) {
			throw new WorkspaceOpenException("IO error checking for workspace lock " + e.getMessage());
		}
		try {
			loc.lock();
		} catch (IOException e) {
			throw new WorkspaceOpenException("Failed to acquire workspace lock " + e.getMessage());
		}
		
		if(!openModelWorkspace(loc.getURL())) {
			throw new WorkspaceOpenException("Failed to open workspace database. If you have workspaces created by an older or beta version " +
					"of netifera, remove the .netifera folder from your home directory and try again");
		}
		return true;
	}
	
	/**
	 * Given a Location instance sets it to the workspace's location
	 * 
	 * @param location
	 *            output parameter, this Location instance will be set to the
	 *            workspace location URL.
	 * @return true if the location could be set
	 */
	private boolean setLocation(Location location) {
		final WorkspaceChooser workspaceChooser = new WorkspaceChooser();
		final URL workspace = workspaceChooser.choose();
		if(workspace == null) {
			return false;
		}
		try {
			location.set(workspace, false);
		} catch(IllegalStateException e) {
			throw new WorkspaceOpenException("Internal error, Location is already set.");
		} catch(IOException e) {
			throw new WorkspaceOpenException("IO Error setting workspace location: " + e.getMessage());
		}
		return true;
	}

	private boolean openModelWorkspace(URL base) {
		return ApplicationPlugin.getDefault().getModel().openWorkspace(base.getPath());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}
