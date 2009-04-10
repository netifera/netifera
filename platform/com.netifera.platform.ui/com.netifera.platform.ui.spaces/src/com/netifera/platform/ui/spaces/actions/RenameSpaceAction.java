package com.netifera.platform.ui.spaces.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import com.netifera.platform.api.model.ISpace;

public class RenameSpaceAction extends Action {
	private ISelectionProvider selectionProvider;
	private ISpace space;

	/* set the listener to change action state based on selection */
	private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			setEnabled(shouldEnable(selection));
		}
	};
	
	public RenameSpaceAction(ISelectionProvider selectionProvider) {
		setSelectionProvider(selectionProvider);
		space = null;
		setId("renameSpaceAction");
		setText("Rename space");
	}

	public void setSelectionProvider(ISelectionProvider selectionProvider) {
		if(this.selectionProvider != null) {
			this.selectionProvider.removeSelectionChangedListener(selectionListener);
		}
		this.selectionProvider = selectionProvider;
		selectionProvider.addSelectionChangedListener(selectionListener);
	}

	public void run() {
		if(space != null) {
			renameSpace(space);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean shouldEnable(ISelection selection) {
		boolean enabled = false;
		if(selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection)selection;
			if(!sel.isEmpty()) {
				enabled = true;
				List<ISpace> elements = sel.toList();
				space = elements.get(0);
			}
		}
		return enabled;
	}

	/**
	 * Ask the user for a new name
	 * @param name current space name
	 * @return new name string or current name if the user provided one is invalid
	 */
	private String askName(final String name) {
		final InputDialog dialog = new InputDialog(null,"Rename space", "Type a new name for the space", name,null);
		dialog.create();

		if( dialog.open() == 0) {
			final String newName = dialog.getValue();
			if(newName != null && newName.length() > 0) {
				return newName;
			}
		}
		return name;
	}

	private void renameSpace(ISpace space) {
		final String name = space.getName();    
		final String newName = askName(name);

		if(!name.equals(newName)) {  
			space.setName(newName);
		}
	}
}