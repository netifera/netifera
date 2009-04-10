package com.netifera.platform.ui.tasks.list;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.ISpaceTaskChangeEvent;
import com.netifera.platform.api.tasks.ITaskRecord;
import com.netifera.platform.ui.updater.StructuredViewerUpdater;

/**
 * @author kevin
 *
 */
public class TaskContentProvider implements IStructuredContentProvider  {

	private ISpace currentSpace;
	private StructuredViewer viewer;
	private StructuredViewerUpdater updater;
	private static final Object[] NO_ELEMENTS = new Object[0];
	private TasksView view;
	
	final private IEventHandler taskChangeHandler = new IEventHandler() {
			public void handleEvent(IEvent event) {
				if(event instanceof ISpaceTaskChangeEvent) {
					handleTaskChanged((ISpaceTaskChangeEvent) event);
				}
			}	
		};

	public TaskContentProvider(TasksView view) {
		this.view = view;
	}
	
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof ISpace) {
			return ((ISpace)inputElement).getTasks().toArray();
		}

		return NO_ELEMENTS;
	}

	public void dispose() {
		if(currentSpace != null) {
			currentSpace.removeTaskChangeListener(taskChangeHandler);
		}	
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(!(newInput instanceof ISpace) || (newInput == oldInput) || viewer == null) {
			return;
		}
		
		if(currentSpace != null) {
			currentSpace.removeTaskChangeListener(taskChangeHandler);
			currentSpace = null;
		}
		
		if(this.viewer != viewer) {
			if(!(viewer instanceof StructuredViewer)) {
				throw new IllegalArgumentException("TaskContentProvider should be used with Structured viewers only");
			}
			this.viewer = (StructuredViewer)viewer;
			/* get update wrapper for the viewer, creating it the first time */
			updater =  StructuredViewerUpdater.get(this.viewer);
		}
	
		currentSpace = (ISpace) newInput;
		currentSpace.addTaskChangeListener(taskChangeHandler);
		
		updateViewActivity(null);
	}
	
	private void handleTaskChanged(final ISpaceTaskChangeEvent event) {
		if(viewer.getControl().isDisposed()) {
			dispose();
			return;
		}
		if(event.isCreationEvent()) {
			updater.refresh();
		}
		else if(event.isUpdateEvent()){
			updater.refresh(event.getTask());
		}

		updateViewActivity(event.getTask());
	}
	
	private void updateViewActivity(final ITaskRecord task) {
		if (view == null)
			return;
		updater.asyncExec(new Runnable() {
			public void run() {
				if (task != null && (task.isRunning() || task.isWaiting())) {
					view.setActive(true);
				} else {
					for (ITaskRecord any: currentSpace.getTasks()) {
						if (any.isRunning() || any.isWaiting()) {
							view.setActive(true);
							return;
						}
					}
					view.setActive(false);
				}
			}
		});
	}
}
