package com.netifera.platform.ui.tasks.list;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ViewPart;

import com.netifera.platform.ui.internal.tasks.TasksPlugin;
import com.netifera.platform.ui.spaces.SpaceEditorInput;
import com.netifera.platform.ui.tasks.actions.TaskCancelAction;
import com.netifera.platform.ui.updater.StructuredViewerUpdater;
import com.netifera.platform.ui.util.ColumnViewerFieldComparator;
import com.netifera.platform.ui.util.FieldViewerComparator;
import com.netifera.platform.ui.util.SelectionProviderProxy;
import com.netifera.platform.ui.util.ViewerRefreshAction;

public class TasksView extends ViewPart {

	public static final String ID = "com.netifera.platform.ui.views.Tasks"; //$NON-NLS-1$

	private static String ACTIVE_ICON = "icons/lightbulb.png";
	private static String INACTIVE_ICON = "icons/lightbulb_off.png";
	
	private StructuredViewer viewer;

	private ViewerRefreshAction viewerRefreshAction = new ViewerRefreshAction();
	private TaskCancelAction taskCancelAction; 
	
	private MenuManager contextMenu;
	private boolean tableMode = false;
	private Composite parent;

	private SelectionProviderProxy selectionProvider;
	private IPartListener partListener;
	private ISelection selection;

	private TaskFilter viewerFilter;
	private TaskRecordFieldComparator viewerComparator;
	

	/**
	 * Create contents of the view part
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;

		/*
		 * publish a proxy selection provider to be able to switch the viewer
		 * and keep the added listeners
		 */
		selectionProvider = new SelectionProviderProxy();
		getSite().setSelectionProvider(selectionProvider);
		viewer = createViewer(parent, tableMode);

		initializeToolBar();
		initializeMenu();
		initializeListeners();
		editorChanged();
	}

	public void dipose() {
		super.dispose();
	}

	private StructuredViewer createViewer(Composite parent, boolean tableMode) {
		StructuredViewer viewer = tableMode ? createTableViewer(parent)
				: createItemViewer(parent);

		viewer.setLabelProvider(new TaskLabelProvider());
		viewer.setContentProvider(new TaskContentProvider(this));
		viewerRefreshAction.setViewer(viewer);
		
		viewerFilter = new TaskFilter();
		viewer.addFilter(viewerFilter);

		/* only set context menu in tableMode */
		if (tableMode) {
			contextMenu = createContextMenu();
			setContextMenu(contextMenu, viewer);
		}

		this.tableMode = tableMode;

		/*
		 * change the selection provider in the published proxy keeping the
		 * selection
		 */
		// FIXME it doesn't work, the selection is not really set, fix
		if (selection != null && !selection.isEmpty()) {
			viewer.setSelection(selection, true);
		}
		selectionProvider.setSelectionProvider(viewer);
		
		if (taskCancelAction == null) {
			taskCancelAction = new TaskCancelAction(selectionProvider);
		}
		
		return viewer;
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

	private void fillContextMenu(IMenuManager menuMgr) {

		menuMgr.add(taskCancelAction);
		menuMgr.add(new ViewerRefreshAction(viewer));

		/* create element filtering configuration menu */
		MenuManager filterMgr = new MenuManager("Show", "filter");
		filterMgr.setRemoveAllWhenShown(true);

		filterMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager m) {
				viewerFilter.fillFilterMenu(m);
			}
		});
		
		viewerFilter.fillFilterMenu(filterMgr);
		/* add the filter selection menu to the context menu */
		menuMgr.add(filterMgr);
		/* add standard separator to handle additions */
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void setContextMenu(MenuManager menuMgr, Viewer viewer) {
		Control viewerControl = viewer.getControl();
		Menu menu = menuMgr.createContextMenu(viewerControl);
		viewerControl.setMenu(menu);
		/* register the pop-up menu using viewer selection provider */
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void initializeListeners() {
		final IWorkbenchWindow window = getSite().getWorkbenchWindow();
		if(window == null) {
			return;
		}
		
		partListener = createPartListener();
		window.addPageListener(createPageListener());
		final IWorkbenchPage page = window.getActivePage();
		if(page != null) {
			page.addPartListener(partListener);
		}
	}
	
	private IPartListener createPartListener() {
		return new IPartListener() {
			public void partActivated(IWorkbenchPart part) {
				if(part instanceof EditorPart) 
					editorChanged();
			}

			public void partClosed(IWorkbenchPart part) {
				if(part instanceof EditorPart) 
					editorChanged();			
			}
			
			public void partOpened(IWorkbenchPart part) {
				if(part instanceof EditorPart) 
					editorChanged();
			}
			public void partBroughtToTop(IWorkbenchPart part) {}
			public void partDeactivated(IWorkbenchPart part) {}		
		};	
	}
	
	private IPageListener createPageListener() {
	    /* As pages are opened and closed attach or detach partListener from them */
		return new IPageListener() {
			public void pageActivated(IWorkbenchPage page) {}
			public void pageClosed(IWorkbenchPage page) { page.removePartListener(partListener); }
			public void pageOpened(IWorkbenchPage page) { page.addPartListener(partListener); }
		};
	}
	
	private void editorChanged() {
		final IEditorPart editor = getActiveEditor();
		
		if(editor == null ||
				!(editor.getEditorInput() instanceof SpaceEditorInput)) {
			return;
		}
		final SpaceEditorInput input = (SpaceEditorInput) editor.getEditorInput();
		
		StructuredViewerUpdater.get(viewer).setInput(input.getSpace());
	}
	
	private IEditorPart getActiveEditor() {
		return getSite().getPage().getActiveEditor();
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
		if (tableMode)
			toolbarManager.add(taskCancelAction);
		toolbarManager.add(viewerRefreshAction);
	}

	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager viewMenuMgr = getViewSite().getActionBars()
				.getMenuManager();
		
		/* switch presentation mode action */
		IAction switchMode = new Action("Table presentation mode",
				Action.AS_CHECK_BOX) {
			public void run() {
				parent.setRedraw(false);
				if (viewer != null) {
					/* keep the viewer selection */
					selection = viewer.getSelection();
					viewer.getControl().dispose();
					for (Control c : parent.getChildren()) {
						c.dispose();
					}
				}
				viewer = createViewer(parent, !tableMode);
				parent.layout();
				parent.setRedraw(true);
				editorChanged();
			}
		};
		viewMenuMgr.add(switchMode);
		
		MenuManager filterMgr = new MenuManager("Show", "filter");
		filterMgr.setRemoveAllWhenShown(true);
		filterMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager m) {
				viewerFilter.fillFilterMenu(m);
			}
		});
		viewerFilter.fillFilterMenu(filterMgr);
		/* add the filter selection menu to the context menu */
		viewMenuMgr.add(filterMgr);
		
		MenuManager sortByMgr = new MenuManager("Sort by", "sort");
		sortByMgr.setRemoveAllWhenShown(true);
		sortByMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager m) {
				fillSortByMenu(m);
			}
		});
		fillSortByMenu(sortByMgr);
		viewMenuMgr.add(sortByMgr);

		MenuManager orderMgr = new MenuManager("Order", "order");
		orderMgr.setRemoveAllWhenShown(true);
		orderMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager m) {
				fillOrderMenu(m);
			}
		});
 
		fillOrderMenu(orderMgr);
		viewMenuMgr.add(orderMgr);

		taskCancelAction =  new TaskCancelAction(selectionProvider);
		
		/* add standard separator to handle additions */
		viewMenuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	public void fillSortByMenu(IMenuManager menuMgr) {
		menuMgr.add(createSortAction("Status", TaskRecordFieldComparator.RUNSTATE));
		menuMgr.add(createSortAction("Title", TaskRecordFieldComparator.NAME));
		menuMgr.add(createSortAction("Start time", TaskRecordFieldComparator.START_TIME));
		menuMgr.add(createSortAction("Elapsed time", TaskRecordFieldComparator.ELAPSED_TIME));
	}
	
	private Action createSortAction(final String text, final int sortField) {
		Action action = new Action(text, Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				viewerComparator.setSortBy(sortField);
				if(viewer != null) {
					viewer.refresh();
				}
			}
		};
		boolean checked = viewerComparator.getSortBy().equals(sortField);
		action.setChecked(checked);
		return action;
	}

	public void fillOrderMenu(IMenuManager menuMgr) {
		Action action = new Action("Ascending", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				viewerComparator.setAscending(true);
				if(viewer != null) {
					viewer.refresh();
				}
			}
		};
		action.setChecked(viewerComparator.isAscending());
		menuMgr.add(action);
		
		action = new Action("Descending", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				viewerComparator.setAscending(false);
				if(viewer != null) {
					viewer.refresh();
				}
			}
		};
		action.setChecked(!viewerComparator.isAscending());
		menuMgr.add(action);
	}
	
	@Override
	public void setFocus() {
		if (viewer != null) {
			viewer.getControl().setFocus();
		}
	}

	private ItemViewer createItemViewer(Composite parent) {
		/* setup viewer */
		ItemViewer itemViewer = new ItemViewer(parent, SWT.BORDER);
		itemViewer.setItemProvider(new TaskItemProvider(parent, parent
				.getStyle()));
		
		viewerComparator = new TaskRecordFieldComparator();
		itemViewer.setComparator(new FieldViewerComparator(itemViewer,
				viewerComparator));

		return itemViewer;
	}

	private TableViewer createTableViewer(Composite parent) {
		final Composite tableComposite = new Composite(parent, SWT.NONE);
		final TableViewer tableViewer = new TableViewer(tableComposite, SWT.V_SCROLL);
		final Table table = tableViewer.getTable();

		final TableColumnLayout columnLayout = new TableColumnLayout();
		
		tableComposite.setLayout(columnLayout);
		
		createTableColumns(table, columnLayout);
		
		/** set some table visual properties */
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		/* make a chained field comparator */
		viewerComparator = new TaskRecordFieldComparator(
				new ColumnViewerFieldComparator(tableViewer));
		tableViewer.setComparator(new FieldViewerComparator(tableViewer,
				viewerComparator));
		return tableViewer;
	}
	
	private void createTableColumns(Table table, TableColumnLayout layout) {
		/* create table columns definition */
		final String[] columnNames = new String[] { " ", "Task", "Start",
				"Elapsed Time" };
		final ColumnLayoutData[] columnLayouts = new ColumnLayoutData[] {
				new ColumnPixelData(25, false, false),
				new ColumnWeightData(100),
				new ColumnPixelData(120, true, false),
				new ColumnPixelData(120, true, false)
		};
		
		final int[] columnAlign = new int[] { SWT.CENTER, SWT.LEFT, 
				SWT.CENTER, SWT.CENTER };

		for (int i = 0; i < columnNames.length; i++) {
			TableColumn column = new TableColumn(table, columnAlign[i]);
			column.setText(columnNames[i]);
			/* this data is used as field index by the TableViewerComparator */
			column.setData(i);
			layout.setColumnData(column, columnLayouts[i]);
		}
	}
	
	public void setActive(boolean tasksRunning) {
		if (tasksRunning) {
			setTitleImage(TasksPlugin.getPlugin().getImageCache().get(ACTIVE_ICON));
		} else {
			setTitleImage(TasksPlugin.getPlugin().getImageCache().get(INACTIVE_ICON));
		}
	}
}
