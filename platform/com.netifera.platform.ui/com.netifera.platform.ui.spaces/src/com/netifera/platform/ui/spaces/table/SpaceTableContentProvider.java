package com.netifera.platform.ui.spaces.table;

import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.ISpaceContentChangeEvent;
import com.netifera.platform.ui.updater.TableUpdater;

public class SpaceTableContentProvider
	implements ILazyContentProvider, IStructuredContentProvider {
	
//	private static final boolean SORT = true;

	private ISpace space;
	private AbstractTableViewer viewer;
	private TableUpdater updater;
	private IEventHandler spaceListener;
	
//	private static Comparator<IShadowEntity> shadowEntityComparator = new EntityComparator();
	
	public Object[] getElements(Object input) {
		if (input instanceof ISpace) {
			return ((ISpace)input).getEntities().toArray();
		}
		return new Object[0];
	}

	public void updateElement(int index) {
		if (viewer != null && space != null) {
			int inputSize = space.entityCount();
			/*
			 * this is a hack? if the virtual table is empty as a result of
			 * being filtered then when the filters are removed the table is not
			 * updated and remains empty.
			 */
			if (inputSize == 0) {
				updater.setItemCount(1);
				return;
			}
			updater.setItemCount(inputSize);
			if (index < inputSize) {
				updater.replace(space.getEntities().get(index), index);
			}
		}
	}

	public void dispose() {
		if(updater != null) {
			updater.dispose();
		}
		updater = null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if ((newInput == oldInput) || viewer == null) {
			return;
		}
		
		if(!validInputChange(viewer, newInput)) {
			return;
		}

		if(updater != null) {
			updater.dispose();
		}
		
		if(space != null && spaceListener != null) {
			space.removeChangeListener(spaceListener);
		}

		this.viewer = (AbstractTableViewer) viewer;
		this.space = (ISpace) newInput;

		this.spaceListener = createSpaceListener();
		this.space.addChangeListener(spaceListener);

		/* get update wrapper for the viewer, creating it the first time */
		updater = TableUpdater.get(this.viewer);
		updater.setItemCount(space.entityCount());
	}
	
	private boolean validInputChange(Viewer viewer, Object newInput) {
		return (viewer instanceof AbstractTableViewer) && (newInput instanceof ISpace);
	}
	
	private IEventHandler createSpaceListener() {
		return new IEventHandler() {
			public void handleEvent(IEvent event) {
				if(event instanceof ISpaceContentChangeEvent) {
					if (!((ISpaceContentChangeEvent)event).isUpdateEvent()) {
						updater.setItemCount(space.entityCount());
					} else {
//						updater.refresh(); //XXX is this right?
					}
				}
			}
		};
	}
}
