package com.netifera.platform.host.filesystem.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public class AbstractFileSystemAction extends Action implements ISelectionChangedListener {
	private ISelectionProvider selectionProvider;
	private IStructuredSelection selection;

	public AbstractFileSystemAction(ISelectionProvider selectionProvider) {
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
	
	/*
	 * FIXME
	 * we're not cleaning the selection listener, it seems
	 */
/*	public void dispose() {
		super.disposed();
		if (selectionProvider != null)
			selectionProvider.removeSelectionChangedListener(this);
	}*/
}