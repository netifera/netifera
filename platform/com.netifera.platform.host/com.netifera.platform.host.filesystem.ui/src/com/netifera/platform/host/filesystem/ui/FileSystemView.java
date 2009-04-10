package com.netifera.platform.host.filesystem.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import com.netifera.platform.host.filesystem.File;
import com.netifera.platform.host.filesystem.IFileSystem;
import com.netifera.platform.host.filesystem.LocalFileSystem;
import com.netifera.platform.host.filesystem.ui.actions.DeleteAction;
import com.netifera.platform.host.filesystem.ui.actions.RenameAction;
import com.netifera.platform.ui.util.TreeAction;
import com.netifera.platform.ui.util.ViewerRefreshAction;


public class FileSystemView extends ViewPart {
	public final static String ID = "com.netifera.platform.ui.views.FileSystem";
	
	private TreeViewer viewer;
	private FileSystemContentProvider contentProvider;

	private ViewerRefreshAction refreshAllAction;
	
	private ViewerRefreshAction refreshAction;
	private DeleteAction deleteAction;
	private RenameAction renameAction;

	/**
	 * Initialize the view.
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
		
		TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText("Name");
		column.getColumn().setWidth(250);
		column.getColumn().setAlignment(SWT.RIGHT);
		column.setLabelProvider(new FileSystemLabelProvider(0));
		
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText("Size");
		column.getColumn().setWidth(70);
		column.getColumn().setAlignment(SWT.RIGHT);
		column.setLabelProvider(new FileSystemLabelProvider(1));

		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText("Last Modified");
		column.getColumn().setWidth(100);
		column.setLabelProvider(new FileSystemLabelProvider(2));

		contentProvider = new FileSystemContentProvider();
		contentProvider.setView(this);
		viewer.setContentProvider(contentProvider);
		
//		viewer.setCellEditors(new CellEditor[] { new TextCellEditor(viewer.getTree()) });
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				File file = (File) ((IStructuredSelection)event.getSelection()).getFirstElement();
				if (file.isDirectory()) {
					if (viewer.getExpandedState(file)) {
						viewer.collapseToLevel(file, 1);
					} else {
						viewer.expandToLevel(file, 1);						
					}
				}
			}
		});

		setupDND();
		createActions();
		createContextMenu();
		
		initializeToolBar();
		
//		setInput(new LocalFileSystem());
	}
	
	public void setInput(IFileSystem input) {
		setPartName(input.toString());
		viewer.setInput(input);
	}
	
	public void setName(String name) {
		setPartName(name);
	}
	
	private void setupDND() {
/*//		viewer.addDragSupport(ops, transfers, new FileDragListener(viewer));
		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { FileTransfer.getInstance() }, new ViewerDropAdapter(viewer) {
			@Override
			public boolean performDrop(Object data) {
				for (String fileName: (String[])data) {
//					System.out.println("file system drop "+fileName);
				}
				// TODO Auto-generated method stub
				return false;
			}
			@Override
			public boolean validateDrop(Object target, int operation, TransferData transferData) {
				if (target instanceof File)
					return ((File)target).isDirectory();
				return false;
			}
		});
*/	}
	
	private void createActions() {
		refreshAllAction = new ViewerRefreshAction(viewer) {
			public void run() {
				contentProvider.clear();
				super.run();
			}
		};
		refreshAllAction.setText("Refresh All");

		refreshAction = new ViewerRefreshAction(viewer) {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection.getFirstElement() instanceof File) {
					File dir = (File) selection.getFirstElement();
					if (dir.isDirectory()) {
						contentProvider.clear(dir);
						super.run();
						return;
					}
				}
				contentProvider.clear();
				super.run();
			}
		};

		deleteAction = new DeleteAction(viewer);
		renameAction = new RenameAction(viewer);
	}
	
	/**
	 * Initialise right-click menu, but does not display it. 
	 */
	private void createContextMenu() {
		MenuManager menuManager = new MenuManager("#PopupMenu");
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		/*
		 * Register so that other plugins can contribute to the menu
		 */
		getSite().registerContextMenu(menuManager, viewer);
	}
	
	/**
	 * Called just before right-click context menu is displayed.
	 * This is where the menu is dynamically built depending on
	 * the current selection and other state information.
	 * 
	 * @param menuManager menu for adding actions to.
	 */
	private void fillContextMenu(IMenuManager menuManager) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection.size() != 1)
			return;
		Object obj = selection.getFirstElement();
		if(obj instanceof File) {
			if (((File) obj).isDirectory())
				menuManager.add(refreshAction);
			
			menuManager.add(renameAction);
			menuManager.add(deleteAction);
		}
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/**
	 * Set GUI focus on correct widget when view is selected.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		Action collapseAll = TreeAction.collapseAll(viewer);
		toolBarManager.add(collapseAll);
		
		toolBarManager.add(refreshAllAction);
	}
	
	public void showMessage(String message) {
		setContentDescription(message);
	}
}