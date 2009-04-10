package com.netifera.platform.host.processes.ui;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

import com.netifera.platform.host.internal.processes.ui.Activator;
import com.netifera.platform.host.processes.Process;

public class ProcessLabelProvider extends ColumnLabelProvider {

	private int column;
	
	public ProcessLabelProvider(int column) {
		this.column = column;
	}

	public Image getImage(Object element) {
		if (!(element instanceof Process))
			return null;
		
		Process process = (Process) element;
		switch(column) {
		case 0:
			String base = "icons/program.png";
			String name = process.getName();
			if (name.matches("bash|sh|.sh"))
				base = "icons/shell.png";
/*			if (name.matches("netstat|ps|top|clam.*|who|w"))
				base = "icons/admin.png";
*/			if (name.matches("sshd|ftpd|httpd|apache|http|ftpd|in\\..*|"))
				base = "icons/server.png";
			if (name.matches("ssh|ftp|firefox.*|telnet|xchat|wget|gaim.*|pidgin.*"))
				base = "icons/client.png";
			if (process.isPriviledged()) {
				String overlayKeys[] = new String[5];
				overlayKeys[IDecoration.TOP_RIGHT] = "icons/priviledged_overlay.png";
				return Activator.getInstance().getImageCache().getDecorated(base, overlayKeys);
			}
			return Activator.getInstance().getImageCache().get(base);
		case 2:
			if (process.getState() == Process.RUNNING)
				return Activator.getInstance().getImageCache().get("icons/process_running.png");
//			if (process.getState() == Process.SLEEPING)
//				return Activator.getInstance().getImageCache().get("icons/process_sleeping.png");
			if (process.getState() == Process.ZOMBIE)
				return Activator.getInstance().getImageCache().get("icons/process_zombie.png");
			break;
		}
		
		return null;
	}
	
	public String getText(Object element) {
		if (!(element instanceof Process)) {
			if (column == 0)
				return element.toString(); // to show "Loading.."
			return null;
		}
		
		Process process = (Process) element;

		switch(column) {
		case 0:
			if (process.getCommandLine() == null)
				return "["+process.getName()+"]";
			else
				return process.getCommandLine();
		case 1:
			return process.getPID()+"";
		case 2:
			if (process.getState() == Process.RUNNING)
				return "R";
			if (process.getState() == Process.SLEEPING)
				return "S";
			if (process.getState() == Process.ZOMBIE)
				return "Z";
			return "?";
		case 3:
			return process.getUID() == 0 ? "root" : process.getUID()+"";
		case 4:
			return getSizeText(process.getSize());
		}
		return null;
	}

	private String getSizeText(long size) {
		if (size == 0)
			return null;
		if (size < 1024) return size+" bytes";
		if (size < 1024*1024) return (size/1024)+" KB";
		if (size < 1024*1024*1024) return (size/1024/1024)+" MB";
		if (size < 1024*1024*1024*1024) return (size/1024/1024/1024)+" GB";
		return size+" bytes";
	}

	public String getToolTipText(Object element) {
		if (!(element instanceof Process))
			return null;
		return ((Process)element).getCommandLine();
	}
}
