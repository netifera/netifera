package com.netifera.platform.ui.util;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;

/**
 * FieldComparator that uses a ColumnViewer label provider to compare object
 * fields.
 * 
 * @author kevin
 *
 */
public class ColumnViewerFieldComparator extends AbstractFieldComparator {

	private final ColumnViewer viewer;

	public ColumnViewerFieldComparator(ColumnViewer viewer) {
		super();
		this.viewer = viewer;
	}

	@Override
	public int compare(Object o1, Object o2) {
		int result = 0;
		
		if (!(sortByField instanceof Integer)) {
			return result;
		}
		
		int sortByFieldIndex = (Integer)sortByField;
		
		IBaseLabelProvider baseLabel = viewer.getLabelProvider();
		if (baseLabel instanceof ITableLabelProvider) {
			ITableLabelProvider tableProvider = (ITableLabelProvider) baseLabel;
			String o1p = tableProvider.getColumnText(o1, sortByFieldIndex);
			String o2p = tableProvider.getColumnText(o2, sortByFieldIndex);
			result = o1p.compareTo(o2p);
		}
		return ascending ?  result : -result;
	}
}
