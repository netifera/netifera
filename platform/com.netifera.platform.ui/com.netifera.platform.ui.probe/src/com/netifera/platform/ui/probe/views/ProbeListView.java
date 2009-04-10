package com.netifera.platform.ui.probe.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.ui.probe.Activator;
import com.netifera.platform.ui.probe.actions.ConnectProbeAction;
import com.netifera.platform.ui.probe.actions.DisconnectProbeAction;
import com.netifera.platform.ui.probe.actions.OpenSpaceAction;

public class ProbeListView extends ViewPart {

	private TableViewer viewer;
	private Action connectProbeAction;
	private Action disconnectProbeAction;
	
	public ProbeListView() {
	}

	@Override
	public void createPartControl(final Composite parent) {
		viewer = createViewer(parent);
		
		Activator.getDefault().getProbeManager().addProbeChangeListener(
				createProbeChangeHandler(parent.getDisplay()));
		
		createToolbarButtons();
	}

	private TableViewer createViewer(Composite parent) {
		final TableViewer tv = new TableViewer(parent, SWT.V_SCROLL | SWT.FULL_SELECTION);
		TableViewerColumn column = new TableViewerColumn(tv, SWT.NONE);
		column.getColumn().setWidth(20);
		column = new TableViewerColumn(tv, SWT.NONE);
		column.getColumn().setWidth(380);
		tv.setContentProvider(new ProbeListContentProvider());
		tv.setLabelProvider(new ProbeListLabelProvider());
		tv.setInput(Activator.getDefault().getProbeManager());
		return tv;
	}
	
	private IEventHandler createProbeChangeHandler(final Display display) {
		return new IEventHandler() {
			public void handleEvent(IEvent event) {
				display.asyncExec(new Runnable() {
					public void run() {
						viewer.refresh();
						setActionEnableStates();
					}
				});
			}
		};
	}
	
	private void createToolbarButtons() {
		final IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		
		connectProbeAction = new ConnectProbeAction(viewer);
		toolBarManager.add(connectProbeAction);
		
		disconnectProbeAction = new DisconnectProbeAction(viewer);
		toolBarManager.add(disconnectProbeAction);

		toolBarManager.add(new OpenSpaceAction(this));
		
		setActionEnableStates();
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				setActionEnableStates();
			}
			
		});
	}
	
	private void setActionEnableStates() {
		connectProbeAction.setEnabled(getConnectActionState());
		disconnectProbeAction.setEnabled(getDisconnectActionState());
	}
	
	private boolean getConnectActionState() {
		final IProbe probe = getSelectedProbe();
		if(probe == null) {
			return false;
		}
		
		return probe.isDisconnected();
	}
	
	private boolean getDisconnectActionState() {
		final IProbe probe = getSelectedProbe();
		if(probe == null) 
			return false;
		
		return probe.isConnected() && !probe.isLocalProbe();
	}
	
	public IProbe getSelectedProbe() {
		final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if(selection == null) 
			return null;
		final Object element = selection.getFirstElement();
		if(!(element instanceof IProbe))
			return null;
		else 
			return (IProbe) element;
	}
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
