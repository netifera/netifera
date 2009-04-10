package com.netifera.platform.net.daemon.sniffing.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.net.daemon.sniffing.ISniffingDaemon;
import com.netifera.platform.net.sniffing.ICaptureFileInterface;

public class CaptureFileAction extends Action {
	public final static String ID = "capture-file-action";
	private final SniffingActionManager manager;
	
	CaptureFileAction(SniffingActionManager manager) {
		this.manager = manager;
		setId(ID);
		setToolTipText("Open A Packet Capture File");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, "icons/open_capfile.png"));
	}
	
	public void run() {
		
		final ISpace space = Activator.getDefault().getCurrentSpace();
		if(space == null) {
			return;
		}
		
		final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		manager.disableAll();
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setText("Select a PCAP capture file to open");
		String path = dialog.open();
		if(path == null) {
			manager.setState();
			return;
		}
		
		final ISniffingDaemon daemon = Activator.getDefault().getSniffingDaemon();
		
		final ICaptureFileInterface iface = daemon.createCaptureFileInterface(path);
		
		if(!iface.isValid()) {
			MessageDialog.openError(shell, "Open '" + path + "' failed.", iface.getErrorMessage());
			manager.setState();
			return;
		}
				
		CaptureFileRunnable runnable = new CaptureFileRunnable(iface, daemon, manager, space);
		try {
			new ProgressMonitorDialog(shell).run(true, true, runnable);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			daemon.cancelCaptureFile();
		}
	}
}