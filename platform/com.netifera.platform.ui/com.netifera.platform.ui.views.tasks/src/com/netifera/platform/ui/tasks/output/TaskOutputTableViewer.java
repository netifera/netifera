package com.netifera.platform.ui.tasks.output;

import java.util.Iterator;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

import com.netifera.platform.api.tasks.ITaskRecord;
import com.netifera.platform.ui.tasks.actions.TaskCancelAction;
import com.netifera.platform.ui.updater.ScrollLockAction;
import com.netifera.platform.ui.updater.TableUpdater;
import com.netifera.platform.ui.util.HookingViewerComparator;
import com.netifera.platform.ui.util.ViewerRefreshAction;

public class TaskOutputTableViewer extends Viewer implements ISelectionListener {

	protected final TableViewer tableViewer;
	protected final Composite control;
	protected final TableUpdater updater;
	private TaskOutputContentProvider contentProvider;
	private TaskOutputFilter viewerFilter;
	private TaskOutputTableLabelProvider labelProvider;
	private IViewPart view;
	private TaskCancelAction taskCancelAction;
	
	public TaskOutputTableViewer(final Composite parent, int style, boolean header, IViewPart view) {
		this(parent,style,header);
		setViewSite(view);
	}
	
	public TaskOutputTableViewer(final Composite parent, int style, boolean header) {
		control = new Composite(parent,style);
		tableViewer = createTableViewer(control);
		updater = TableUpdater.get(tableViewer);
		addContextMenu(tableViewer);
		tableViewer.getTable().setHeaderVisible(header);
		tableViewer.getControl().addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				parent.setFocus();
			}
			public void focusLost(FocusEvent e) {

			}
		});
	}
	
	/**
	 * Allows to set the ViewPart instance where the viewer is contained,
	 * allowing the viewer to set properties of the site based on the content
	 * being shown by the viewer.
	 * 
	 * @param view
	 *            the ViewPart where this viewer is contained
	 */
	public void setViewSite(IViewPart view) {
		this.view = view;
	}
	
	private TableViewer createTableViewer(Composite parent) {
		parent.setLayout(new FillLayout());
		final TableViewer tableViewer = new TableViewer(control, SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		contentProvider = new TaskOutputContentProvider();
		tableViewer.setContentProvider(contentProvider);
		labelProvider = new TaskOutputTableLabelProvider();
		tableViewer.setLabelProvider(labelProvider);
		viewerFilter = new TaskOutputFilter();
		tableViewer.addFilter(viewerFilter);

		final Table table = tableViewer.getTable();
		final TableColumnLayout columnLayout = new TableColumnLayout();
		control.setLayout(columnLayout);
		createTableColumns(table, columnLayout);		
		
		tableViewer.setComparator(new HookingViewerComparator(tableViewer){

			@Override
			public void setAscending(boolean ascending) {
				contentProvider.setAscending(ascending);
			}

			@Override
			public void setSortBy(Object fieldId) {
				//ignore field now to test concept
				
			}});
		/* set some table visual properties */
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		return tableViewer;
	}
	
	private void createTableColumns(Table table, TableColumnLayout layout) {
		final String[] columnNames = new String[] { "Time",  "Message" };
		final int[] columnAlign = new int[] { SWT.LEFT, SWT.LEFT };
		final ColumnLayoutData[] columnLayouts = new ColumnLayoutData[] {
				new ColumnPixelData(70, true, false),
				new ColumnWeightData(100, 300, true)
		};
		
		for(int i = 0; i < columnNames.length; i++) {
			TableColumn column = new TableColumn(table, columnAlign[i]);
			column.setText(columnNames[i]);
			column.setData(i);
			layout.setColumnData(column, columnLayouts[i]);
		}
	}
	
	private MenuManager addContextMenu(Viewer viewer) {
		/*create popup menu manager */
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(false);

		/* create element filtering configuration menu */
		MenuManager filterMgr = new MenuManager("Show","filter");
		filterMgr.setRemoveAllWhenShown(true);
		
		filterMgr.addMenuListener(new IMenuListener() { 
			public void menuAboutToShow(IMenuManager m) {
				viewerFilter.fillFilterMenu(m);
			}
		});
		viewerFilter.fillFilterMenu(filterMgr);

		menuMgr.add(new ViewerRefreshAction(tableViewer));

		menuMgr.add(filterMgr);		

		menuMgr.add(new ScrollLockAction(updater));

		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		/* add menu as viewer's context menu */
		Control viewerControl = viewer.getControl();
		Menu menu = menuMgr.createContextMenu(viewerControl);
		viewerControl.setMenu(menu);		
		
		//How to register without site involved?
		//getSite().registerContextMenu(menuMgr, viewer);
		return menuMgr;
	}

	@SuppressWarnings("unchecked")
	private void setSelectedTask(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		ITaskRecord taskRecord = null;
		IStructuredSelection sel = (IStructuredSelection) selection;
		Iterator<Object> iter = sel.iterator();
		if (iter != null && iter.hasNext()) {
			Object selected = iter.next();
			if (selected instanceof ITaskRecord) {
				taskRecord = (ITaskRecord) selected;
			}
		}

		/* if the table is inside a view update the view's toolbar */
		if(view != null) {
			if(taskCancelAction != null) {
				view.getViewSite().getActionBars().getToolBarManager().remove(taskCancelAction.getId());
			}
			if(taskRecord != null) {
				taskCancelAction = new TaskCancelAction(taskRecord);
				view.getViewSite().getActionBars().getToolBarManager().add(taskCancelAction);
				view.getViewSite().getActionBars().getToolBarManager().update(false);
			}
		}
		setInput(taskRecord);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		setSelectedTask(selection);
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
    public void setInput(Object input) {
		if(input == null || !input.equals(tableViewer.getInput())) {
			updater.setInput(input);
		}
	}

	@Override
    public Object getInput() {
		return tableViewer.getInput();
	}

	@Override
    public ISelection getSelection() {
		return tableViewer.getSelection();
	}

	@Override
    public void refresh() {
		updater.refresh();
	}

	@Override
    public void setSelection(ISelection selection, boolean reveal) {
		tableViewer.setSelection(selection,reveal);
	}
}
