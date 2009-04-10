package com.netifera.platform.ui.spaces.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.ISpaceStatusChangeEvent;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.ui.updater.StructuredViewerUpdater;

public class SpaceListContentProvider implements IStructuredContentProvider, IEventHandler {

	private IWorkspace workspace;
	private StructuredViewerUpdater updater;

	public Object[] getElements(Object inputElement) {
		List<ISpace> spaces = new ArrayList<ISpace>( workspace.getAllSpaces());
		Collections.sort(spaces, new Comparator<ISpace>() {

			public int compare(ISpace o1, ISpace o2) {
				if(o1.getId() > o2.getId()) {
					return 1;
				} else if(o1.getId() < o2.getId()) {
					return -1;
				} else {
					return 0;
				}
			}

		});
		return spaces.toArray();
	}

	public void dispose() {
		updater = null;
		if(workspace != null) {
			workspace.removeSpaceCreationListener(this);
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(newInput instanceof IWorkspace) {
			
			/* if workspace change remove handler from old and add to new */
			final IWorkspace workspace = (IWorkspace) newInput;
			if(this.workspace != workspace) {
				if(this.workspace != null) {
					this.workspace.removeSpaceCreationListener(this);
				}
				workspace.addSpaceCreationListener(this);
				this.workspace = workspace;
			} 
			
			updater = StructuredViewerUpdater.get((StructuredViewer) viewer);
			
		}		
	}
	public void handleEvent(final IEvent event) {
		if(event instanceof ISpaceStatusChangeEvent) {
			final ISpaceStatusChangeEvent spaceChange = (ISpaceStatusChangeEvent) event;
			updater.refresh(spaceChange.getSpace());
		}
	}
}
