package com.netifera.platform.host.processes.ui;


import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.host.internal.processes.ui.Activator;
import com.netifera.platform.host.processes.IProcessManager;
import com.netifera.platform.host.processes.IProcessManagerFactory;
import com.netifera.platform.host.processes.Process;
import com.netifera.platform.host.processes.ui.actions.KillAction;
import com.netifera.platform.host.processes.ui.actions.ToggleTreeModeAction;
import com.netifera.platform.ui.util.ViewerRefreshAction;

public class ProcessListView extends ViewPart {
	public final static String ID = "com.netifera.platform.ui.views.ProcessList";

	private ToggleTreeModeAction toggleTreeModeAction;
	private ViewerRefreshAction refreshAction;
	
	private KillAction killAction;
	
	private TreeViewer viewer;
	private ProcessListContentProvider contentProvider;


	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
	
		TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText("Name");
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ProcessLabelProvider(0));

		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText("State");
		column.getColumn().setWidth(50);
		column.setLabelProvider(new ProcessLabelProvider(2));
		
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText("PID");
		column.getColumn().setWidth(70);
		column.getColumn().setAlignment(SWT.RIGHT);
		column.setLabelProvider(new ProcessLabelProvider(1));

		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText("User");
		column.getColumn().setWidth(70);
		column.getColumn().setAlignment(SWT.LEFT);
		column.setLabelProvider(new ProcessLabelProvider(3));

		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText("Memory");
		column.getColumn().setWidth(70);
		column.getColumn().setAlignment(SWT.LEFT);
		column.setLabelProvider(new ProcessLabelProvider(4));

		contentProvider = new ProcessListContentProvider();
		viewer.setContentProvider(contentProvider);
		
		createActions();
		createContextMenu();
		
		initializeToolBar();
		
//		setInputToCurrentProbe();
	}
	
	private void setInputToCurrentProbe() {
		IProcessManagerFactory factory = Activator.getInstance().getProcessManagerFactory();
		IProbe probe = Activator.getInstance().getCurrentProbe();
		IProcessManager processManager = factory.createForProbe(probe);
		setInput(processManager);
	}
	
	private void createActions() {
		refreshAction = new ViewerRefreshAction(viewer) {
			public void run() {
				contentProvider.clear();
				super.run();
			}
		};
		
		killAction = new KillAction(viewer);
		
		toggleTreeModeAction = new ToggleTreeModeAction(this);
	}
	
	/**
	 * Initializes the right-click menu, but does not display it. 
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
		
		// Register so that other plugins can contribute to the menu
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
		Object o = selection.getFirstElement();
		if (o instanceof Process) {
			menuManager.add(killAction);
		}
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));		
	}
	
	/**
	 * Set GUI focus on correct widget when view is selected.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(toggleTreeModeAction);
		toolBarManager.add(refreshAction);
	}
	
	public void setName(String name) {
		setPartName(name);
	}
	public void setInput(IProcessManager input) {
		viewer.setInput(input);
	}

	public void setTreeMode(boolean treeMode) {
		if (contentProvider.isTreeMode() == treeMode)
			return;
		contentProvider.setTreeMode(treeMode);
		viewer.refresh();
	}
	
	public boolean isTreeMode() {
		return contentProvider.isTreeMode();
	}
}