/**
 * 
 */
package com.netifera.platform.ui.tasks.list;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.netifera.platform.api.tasks.ITaskRecord;

public class TaskItemProvider implements IItemProvider {

	private ILabelProvider labelProvider;
	private Composite parent;
	private int style;
	private FormToolkit toolkit = new FormToolkit(Display.getCurrent());

	public TaskItemProvider(Composite parent, int style) {
		this.parent = parent;
		this.style = style;
	}

	public Widget getItem(Object element) {
		if(parent.isDisposed() && !(element instanceof ITaskRecord)) {
			return null;
		}
//		parent.setRedraw(false);
		TaskItem item = new TaskItem(parent, style, toolkit);
		item.setData(element);
		
//		parent.setRedraw(true);
		return item;
	}
	
	public void setLabelProvider(ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	public void updateItem(Widget item, Object element) {
		item.setData(element);
	}

	public void setParent(Composite parent) {
		this.parent = parent;
	}

	public Composite getParent() {
		return parent;
	}

	public void dispose() {
		toolkit.dispose();
		/* nothing else to do here? dispose widgets? */
	}
}
