/**
 * 
 */
package com.netifera.platform.ui.util;

import java.util.Comparator;

/**
 * @author kevin
 *
 */
public abstract class AbstractFieldComparator implements Comparator<Object> {

	protected Object sortByField;
	protected boolean ascending = true;

	public Object getSortBy() {
		return sortByField;
	}
	
	public void setSortBy(Object sortByField) {
		this.sortByField = sortByField;
	}
	
	public boolean isAscending() {
		return ascending;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public abstract int compare(Object o1, Object o2);
}
