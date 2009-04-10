package com.netifera.platform.net.wifi.ui.views;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.ViewPart;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.net.model.InternetAddressEntity;
import com.netifera.platform.net.wifi.model.WirelessStationEntity;
import com.netifera.platform.net.wifi.ui.Activator;
import com.netifera.platform.net.wifi.ui.ISpaceChangeListener;
import com.netifera.platform.ui.spaces.ISpaceEditor;

public class WirelessView extends ViewPart implements ISpaceChangeListener {
	public static String ID = "com.netifera.platform.views.wifi";
	private TreeViewer treeViewer;
	
	public WirelessView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Activator.getDefault().registerSpaceChangeListener(this);
		treeViewer = new TreeViewer(parent, SWT.NONE);
		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.setLabelProvider(new WirelessLabelProvider());
		treeViewer.setContentProvider(new WirelessContentProvider());
		treeViewer.setInput(Activator.getDefault().getCurrentSpace());
		
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				if(event.getSelection() instanceof IStructuredSelection)
					doubleClickHandler((IStructuredSelection) event.getSelection());				
			}
			
		});
	}

	private void doubleClickHandler(IStructuredSelection selection) {
		
		if(!(selection.getFirstElement() instanceof IEntity))
			return;
		
		focusWirelessEntity((IEntity) selection.getFirstElement());
		
	}
	
	private void focusWirelessEntity(IEntity entity) {
		final ISpaceEditor editor = Activator.getDefault().getCurrentSpaceEditor();
		if(editor == null)
			return;
		if(entity instanceof WirelessStationEntity) {
			focusStationEntity(editor, (WirelessStationEntity) entity);
		} else {
			editor.focusEntity(entity);
		}
		
	}
	
	private void focusStationEntity(ISpaceEditor editor, WirelessStationEntity station) {
		if(!(station.getNetworkAddress() instanceof InternetAddressEntity))
			return;
		final InternetAddressEntity address = (InternetAddressEntity) station.getNetworkAddress();
		editor.focusEntity(address.getHost());
	}
	
	@Override
	public void setFocus() {
		treeViewer.getTree().setFocus();
	}

	public void spaceChanged(ISpace space) {
		treeViewer.setInput(space);		
	}

}
