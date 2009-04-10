package com.netifera.platform.ui.spaces.views;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.ui.internal.spaces.Activator;
import com.netifera.platform.ui.spaces.SpaceEditorInput;
import com.netifera.platform.ui.spaces.actions.RenameSpaceAction;
import com.netifera.platform.ui.spaces.editors.SpaceEditor;

public class SpaceListView extends ViewPart {

	private TableViewer viewer;
	private RenameSpaceAction renameSpaceAction;
	
	public SpaceListView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);

		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(150);
		
		viewer.setContentProvider(new SpaceListContentProvider());
		viewer.setLabelProvider(new SpaceListLabelProvider());
		viewer.setInput(Activator.getDefault().getModel().getCurrentWorkspace());
		
		//XXX this kind of updates should be done in the content provider
		Activator.getDefault().getModel().getCurrentWorkspace().addSpaceCreationListener(new IEventHandler() {

			public void handleEvent(IEvent event) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if(!viewer.getControl().isDisposed()) {
								viewer.refresh();
							}
						}
					});
				}
		});

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				handleDoubleClick((ISpace) selection.getFirstElement());
			}
			
		});
		/* create space rename action and add it to context menu */
		renameSpaceAction = new RenameSpaceAction(viewer);
		setContextMenu(createContextMenu(), viewer);
	}
	
	private void fillContextMenu(IMenuManager menuMgr) {
		menuMgr.add(renameSpaceAction);
	}

	private MenuManager createContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager m) {
				fillContextMenu(m);
			}
		});
		return menuMgr;
	}
	
	private void setContextMenu(MenuManager menuMgr, Viewer viewer) {
		Control viewerControl = viewer.getControl();
		Menu menu = menuMgr.createContextMenu(viewerControl);
		viewerControl.setMenu(menu);
		/* register the pop-up menu using viewer selection provider */
		getSite().registerContextMenu(menuMgr, viewer);
	}
	

	private void handleDoubleClick(ISpace space) {
		try {
			if(space.isOpened()) {
				focusEditorForSpace(space);
			} else {
				openEditorForSpace(space);
			}
		} catch(PartInitException e) {
			
		}
	}
	private void openEditorForSpace(ISpace space) throws PartInitException {
		final IEditorInput input = new SpaceEditorInput(space);
		space.open();
		getPage().openEditor(input, SpaceEditor.ID);
	}
	
	private void focusEditorForSpace(ISpace space) throws PartInitException {
		for(IEditorReference reference : getPage().getEditorReferences()) {
			if(reference.getEditorInput() instanceof SpaceEditorInput) {
				SpaceEditorInput input = (SpaceEditorInput) reference.getEditorInput();
				if(input.getSpace() == space) {
					getPage().activate(reference.getEditor(true));
				}
			}
		}
	}
			
	
	
	private IWorkbenchPage getPage() {
		return Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

}
