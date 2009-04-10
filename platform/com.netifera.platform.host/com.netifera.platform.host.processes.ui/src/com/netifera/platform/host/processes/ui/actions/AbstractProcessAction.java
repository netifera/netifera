package com.netifera.platform.host.processes.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public class AbstractProcessAction extends Action implements ISelectionChangedListener {
	private ISelectionProvider selectionProvider;
	private IStructuredSelection selection;

	public AbstractProcessAction(ISelectionProvider selectionProvider) {
		setSelectionProvider(selectionProvider);
	}

	public void setSelectionProvider(ISelectionProvider selectionProvider) {
		if(this.selectionProvider != null) {
			this.selectionProvider.removeSelectionChangedListener(this);
		}
		this.selectionProvider = selectionProvider;
		selectionProvider.addSelectionChangedListener(this);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		selection = (IStructuredSelection) event.getSelection();
		setEnabled(!selection.isEmpty());
	}
	
	public IStructuredSelection getSelection() {
		return selection;
	}
}