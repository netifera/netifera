package com.netifera.platform.host.processes.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.netifera.platform.host.internal.processes.ui.Activator;
import com.netifera.platform.host.processes.IProcessManager;
import com.netifera.platform.host.processes.Process;

;public class ProcessListContentProvider implements ITreeContentProvider {

	private TreeViewer viewer;
	private IProcessManager processManager;
	private Process[] roots;
	private boolean treeMode = false;
	
	public Object[] getChildren(Object o) {
		if (!treeMode || !(o instanceof Process) || (roots == null))
			return new Object[0];
		Process parent = (Process) o;
		List<Process> children = new ArrayList<Process>();
		for (Process process: roots) {
			if (process.getPPID() == parent.getPID())
				children.add(process);
		}
		return children.toArray(new Process[children.size()]);
	}

	public Process getParent(Object o) {
		if (!treeMode || !(o instanceof Process) || (roots == null))
			return null;
		for (Process process: roots)
			if (process.getPID() == ((Process)o).getPPID())
				return process;
		return null;
	}

	public boolean hasChildren(Object o) {
		if (!treeMode || !(o instanceof Process) || (roots == null))
			return false;
		for (Process process: roots)
			if (process.getPPID() == ((Process)o).getPID())
				return true;
		return false;
	}

	public Object[] getElements(final Object input) {
		if(input != processManager) {
			throw new IllegalArgumentException();
		}
		
		if (roots == null) {
			new Thread(new Runnable() {
				public void run() {
					try {
						final Process[] children = processManager.getProcessList();
						viewer.getControl().getDisplay().syncExec(new Runnable() {
							public void run() {
								if (processManager != input)
									return;
								roots = children;
								viewer.refresh(true);
							}
						});
					} catch (Exception e) {
						showException(e);
					}
				}
			}).start();
			return new String[] {"Loading..."};
		}

		if (!treeMode)
			return roots.clone();
		
		List<Process> elements = new ArrayList<Process>();
		for (Process process: roots) {
			if (getParent(process) == null)
				elements.add(process);
		}
		return elements.toArray(new Process[elements.size()]);
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public void clear() {
		roots = null;
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer) viewer;
		processManager = (IProcessManager) newInput;
		clear();
	}
	
	public void setTreeMode(boolean enabled) {
		treeMode = enabled;
	}

	public boolean isTreeMode() {
		return treeMode;
	}

	private void showException(Exception e) {
		final String message = e.getMessage() != null ? e.getMessage() : e.toString();
/*		if (view != null)
			viewer.getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					if (view != null)
						view.showMessage("Error: "+message);
					else
						Activator.getInstance().getBalloonManager().error(message);
				}
			});
		else
*/			Activator.getInstance().getBalloonManager().error(message);
	}
}
