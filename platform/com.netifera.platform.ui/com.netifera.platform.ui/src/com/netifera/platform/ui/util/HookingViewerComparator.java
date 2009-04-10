/**
 * 
 */
package com.netifera.platform.ui.util;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * A ViewerComparator that hooks the viewer control to allow the user to configure 
 * the comparator configuration.
 * 
 * @author kevin
 *
 */
public abstract class HookingViewerComparator extends ViewerComparator {

	protected final Viewer viewer;

	public HookingViewerComparator(Viewer viewer) {
		this.viewer = viewer;
		hookViewer();
	}

	protected void hookViewer() {
		if (viewer instanceof TableViewer) {
			Table table = ((TableViewer) viewer).getTable();
			TableColumn[] columns = table.getColumns();
			for (int i = 0; i < columns.length; i++) {
				TableColumn column = columns[i];
				addColumnSelectionListener(column);
			}
		}
	}

	private void addColumnSelectionListener(final TableColumn column) {
		column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				boolean ascending = true;
				TableColumn selectedColumnTable = (TableColumn) se.widget;
				Table table = selectedColumnTable.getParent();
				TableColumn sortColumnTable = table.getSortColumn();
				table.setRedraw(false);

				if (sortColumnTable != selectedColumnTable) {
					/* show mark in the sort column header */
					table.setSortColumn(selectedColumnTable);
					/* set selected column as sort field */
					setSortBy(selectedColumnTable.getData());

				} else {
					/* if same column only switch direction */
					ascending = (table.getSortDirection() == SWT.UP);
				}
				/* always set ascending */
				setAscending(ascending);
				/* set direction of the mark in the sort column header */
				table.setSortDirection(ascending ? SWT.DOWN : SWT.UP);
				table.setRedraw(true);
			}
		});

	}

	public Viewer getViewer() {
		return viewer;
	}
	
	/**
	 * @param sortByField object to identify the field to be compared. 
	 * Is up to subclasses to interpret its meaning.
	 */
	public abstract void setSortBy(Object sortByField);

	/**
	 * @param ascending the direction of the sort. Ascending or not.
	 */
	public abstract void setAscending(boolean ascending);

}
