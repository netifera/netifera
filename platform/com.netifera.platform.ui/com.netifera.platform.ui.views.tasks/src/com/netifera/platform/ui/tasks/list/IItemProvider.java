package com.netifera.platform.ui.tasks.list;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

public interface IItemProvider {
	
	public void setLabelProvider(ILabelProvider labelProvider);

	public void setParent(Composite parent);

	public Composite getParent();

	public Widget getItem(Object element);

	public void updateItem(Widget item, Object element);
	
	public void dispose();
}
