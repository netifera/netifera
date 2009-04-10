package com.netifera.platform.ui.tasks.output;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.tasks.ITaskOutput;
import com.netifera.platform.api.tasks.ITaskOutputEvent;
import com.netifera.platform.api.tasks.ITaskRecord;
import com.netifera.platform.ui.updater.TableUpdater;

public class TaskOutputContentProvider implements ILazyContentProvider,
IStructuredContentProvider {
	private final IEventHandler taskOutputHandler;

	private ITaskRecord focusedTask;
	private List<ITaskOutput> input;
	private AbstractTableViewer viewer;
	private TableUpdater updater;
	
	// if ascending false updateElement requested indexes are reversed
	private volatile boolean ascending = true;
	private List<Object> filteredElements;

	TaskOutputContentProvider() {
		taskOutputHandler = new IEventHandler() {
			public void handleEvent(IEvent event) {
				if (event instanceof ITaskOutputEvent) {
					handleTaskOutput((ITaskOutputEvent) event);
				}
			}
		};
	}

	private void handleTaskOutput(ITaskOutputEvent event) {
		ITaskOutput message = event.getMessage();
		if ((focusedTask != null)
				&& (focusedTask.getTaskId() == message.getTaskId())) {
			updateFilters(viewer.getFilters());
			addFiltered(message);
			updater.setItemCount(inputSize());
		}
	}

	public void dispose() {
		if(focusedTask != null) {
			focusedTask.removeTaskOutputListener(taskOutputHandler);
		}
	}

	public void updateElement(int index) {
		if (viewer != null && input != null) {
			int inputSize = inputSize();
			/*
			 * this is a hack? if the virtual table is empty as a result of
			 * being filtered then when the filters are removed the table is not
			 * updated and remains empty.
			 */
			if (inputSize == 0) {
				updater.setItemCount(1);
				return;
			}
			updater.setItemCount(inputSize());
			if (index < inputSize) {
				updater.replace(getElement(index), index);
			}
		}
	}

	/**
	 * @param virtualIndex
	 *            the index in the virtual table
	 * @return the element in the given index after sorting and filtering
	 */
	private Object getElement(int virtualIndex) {
		int index = ascending ? virtualIndex : (inputSize() - virtualIndex - 1);

		/* if not filtered input return input element */
		if (filteredElements == null) {
			return input.get(index);
		}

		/* get filters from viewer */
		ViewerFilter[] filters = viewer.getFilters();

		if (filters == null || filters.length == 0) {
			/* if no filters then forget about filtering */
			filteredElements = null;
			return input.get(index);
		}

		return filteredElements.get(index);

	}

	private boolean updateFilters(ViewerFilter[] filters) {
		/* check if filter configuration changed */
		for (ViewerFilter filter : filters) {
			/* it is an innocent hack */
			if (filter.isFilterProperty(null, null)) {
				/* filter again */
				filteredElements = filteredElements();
				return true;
			}
		}
		return false;
	}

	private boolean isFiltered(Object element) {
		ViewerFilter[] filters = viewer.getFilters();

		if (filters == null || filters.length == 0) {
			return false;
		}

		for (ViewerFilter filter : filters) {
			if (!filter.select(viewer, null, element)) {
				return true;
			}
		}
		return false;
	}

	protected List<Object> filteredElements() {
		ViewerFilter[] filters = viewer.getFilters();
		if (filters != null && filters.length > 0) {
			ArrayList<Object> filtered = new ArrayList<Object>(input.size());

			for (Object element : input.toArray()) {
				boolean selected = true;

				for (ViewerFilter filter : filters) {
					selected = filter.select(viewer, null, element);
					if (!selected) {
						break;
					}
				}
				if (selected) {
					filtered.add(element);
				}
			}
			return filtered;
		}
		return null;
	}

	protected void addFiltered(Object element) {
		if (filteredElements != null && !isFiltered(element)) {
			filteredElements.add(element);
		}
	}

	private int inputSize() {

		if (filteredElements != null) {
			updateFilters(viewer.getFilters());
			return filteredElements.size();
		}
		return input == null ? 0 : input.size();
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		if ((newInput == oldInput) || viewer == null) {
			return;
		}

		if (this.viewer != viewer) {
			if (!(viewer instanceof AbstractTableViewer)) {
				throw new IllegalArgumentException(
				"TaskOutputContentProvider should be used with AbstractTableViewer viewers only");
			}
			this.viewer = (AbstractTableViewer) viewer;
			/* get update wrapper for the viewer, creating it the first time */
			updater = TableUpdater.get(this.viewer);
		}

		/* if input is null or not task record clear viewer */
		if (!(newInput instanceof ITaskRecord)) {
			input = null;
			filteredElements = null;
			updater.clear();
			return;
		}


		if(focusedTask != null) {
			focusedTask.removeTaskOutputListener(taskOutputHandler);
			focusedTask = null;
		}
		focusedTask = (ITaskRecord) newInput;
		focusedTask.addTaskOutputListener(taskOutputHandler);

		setTaskOutput(focusedTask.getTaskOutput());
		updater.setItemCount(inputSize());
		
	}

	private void setTaskOutput(List<ITaskOutput> input) {
		this.input = input;
		filteredElements = filteredElements();
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
			return ((List<?>) inputElement).toArray();
		}
		return new Object[0];
	}

	/*
	 * cheap viewer sorting. This method gets called when the user clicks a
	 * table column header
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
		if (updater != null) {
			updater.refresh();
		}
	}
}
