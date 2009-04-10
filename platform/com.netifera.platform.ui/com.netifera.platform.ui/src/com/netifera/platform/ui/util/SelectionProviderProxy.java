package com.netifera.platform.ui.util;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * SelectionProviderProxy keeps a list of listeners allowing to change the real
 * provider. The listeners are not aware of the change. It is used when a part
 * publishes a selection provider from a viewer and later the viewer is changed.
 * In that case a proxy is published in the part creation code and the provider
 * of the proxy is updated in each part's viewer switch.
 * 
 * 
 */
public class SelectionProviderProxy implements IPostSelectionProvider {

	private ISelectionProvider provider;
	
	private final ListenerList postSelectionList = new ListenerList();
	
	private ISelectionChangedListener postSelectionListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			if (event.getSelectionProvider() == provider) {
				firePostSelectionChanged(event.getSelection());
			}
		}
	};
	
	private final ListenerList selectionList = new ListenerList();

	private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			if (event.getSelectionProvider() == provider) {
				fireSelectionChanged(event.getSelection());
			}
		}
	};

	/* StructuredSelection.EMPTY could be used here */
	private final ISelection emptySelection = new ISelection() {

		public boolean isEmpty() {
			return true;
		}
	};
	
	/**
	 * @param newProvider the real provider represented by this proxy 
	 */
	public void setSelectionProvider(ISelectionProvider newProvider) {
		if (provider != null) {
			provider.removeSelectionChangedListener(selectionListener);
			if (provider instanceof IPostSelectionProvider) {
				((IPostSelectionProvider) provider)
						.removePostSelectionChangedListener(postSelectionListener);
			}
		}
		provider = newProvider;

		if (newProvider != null) {
			newProvider.addSelectionChangedListener(selectionListener);
			if (newProvider instanceof IPostSelectionProvider) {
				((IPostSelectionProvider) newProvider)
						.addPostSelectionChangedListener(postSelectionListener);
			}
			fireSelectionChanged(newProvider.getSelection());
			firePostSelectionChanged(newProvider.getSelection());
		}
	}

	public void addPostSelectionChangedListener(
			ISelectionChangedListener listener) {
		postSelectionList.add(listener);
	}

	public void removePostSelectionChangedListener(
			ISelectionChangedListener listener) {
		postSelectionList.remove(listener);
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionList.add(listener);
	}

	public ISelection getSelection() {
		return (provider == null) ? emptySelection : provider.getSelection();
	}

	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		selectionList.remove(listener);
	}

	public void setSelection(ISelection selection) {
		if (provider != null) {
			provider.setSelection(selection);
		}
	}

	protected void fireSelectionChanged(ISelection selection) {
		fireSelectionChanged(selectionList, selection);
	}

	protected void firePostSelectionChanged(ISelection selection) {
		fireSelectionChanged(postSelectionList, selection);
	}

	private void fireSelectionChanged(ListenerList list, ISelection selection) {
		
		if(selection == null) {
			return;
		}
		
		SelectionChangedEvent event = new SelectionChangedEvent(provider,
				selection);
		Object[] listeners = list.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			ISelectionChangedListener listener = (ISelectionChangedListener) listeners[i];
			listener.selectionChanged(event);
		}
	}
}
