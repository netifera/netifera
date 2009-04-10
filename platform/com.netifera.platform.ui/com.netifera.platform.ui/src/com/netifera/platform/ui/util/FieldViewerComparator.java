/**
 * 
 */
package com.netifera.platform.ui.util;

import org.eclipse.jface.viewers.Viewer;

public class FieldViewerComparator extends HookingViewerComparator {

	protected final AbstractFieldComparator fieldComparator;

	public FieldViewerComparator(Viewer viewer, AbstractFieldComparator fieldComparator) {
		super(viewer);
		this.fieldComparator = fieldComparator;
	}
	
	public void setSortBy(Object sortByField) {
		fieldComparator.setSortBy(sortByField);
	}

	public void setAscending(boolean ascending) {
		fieldComparator.setAscending(ascending);
		/* set ascending is always called after setSortBy by the hooker */ 
		viewer.refresh();
	}
	
	public int compare(Viewer viewer, Object e1, Object e2) {
			return fieldComparator.compare(e1, e2);
	}
}
