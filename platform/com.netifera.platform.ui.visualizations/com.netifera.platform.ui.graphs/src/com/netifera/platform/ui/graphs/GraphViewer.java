package com.netifera.platform.ui.graphs;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;

public class GraphViewer extends ContentViewer {

	private GraphControl control;
	
	public GraphViewer(Composite parent) {
		control = new GraphControl(parent);
	}

	@Override
	public GraphControl getControl() {
		return control;
	}

	@Override
	public void refresh() {
		control.setGraph(getContentProvider().getGraph());
	}
	
	public void update() {
		control.redraw();
	}

	public void setLabelProvider(final ColumnLabelProvider labelProvider) {
		super.setLabelProvider(labelProvider);
		control.setNodeLabelProvider(labelProvider);
	}
	
	public void setContentProvider(IGraphContentProvider contentProvider) {
		super.setContentProvider(contentProvider);
	}

	public IGraphContentProvider getContentProvider() {
		return (IGraphContentProvider) super.getContentProvider();
	}
	
	public void inputChanged(Object input, Object oldInput) {
		super.inputChanged(input, oldInput);
		refresh();
	}
	
	@Override
	public void setSelection(ISelection arg0, boolean arg1) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public ISelection getSelection() {
		// TODO Auto-generated method stub
		return null;
	}
}
