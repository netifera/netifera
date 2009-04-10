package com.netifera.platform.net.wifi.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.ISpaceContentChangeEvent;
import com.netifera.platform.net.wifi.model.AccessPointEntity;
import com.netifera.platform.net.wifi.model.ExtendedServiceSetEntity;
import com.netifera.platform.net.wifi.model.WirelessStationEntity;

public class WirelessContentProvider implements ITreeContentProvider {

	private ISpace currentSpace;
	private Viewer viewer;
	private final IEventHandler spaceListener;
	private List<ExtendedServiceSetEntity> networks = new ArrayList<ExtendedServiceSetEntity>();
	
	WirelessContentProvider() {
		spaceListener = createSpaceListener();
	}
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof ExtendedServiceSetEntity) {
			ExtendedServiceSetEntity ess = (ExtendedServiceSetEntity) parentElement;
			List<AccessPointEntity> accessPoints = ess.getAccessPoints();
			List<WirelessStationEntity> stations = ess.getStations();
			List<IEntity> out = new ArrayList<IEntity>(accessPoints);
			out.addAll(stations);
			return out.toArray();
		}
			
		// TODO Auto-generated method stub
		return null;
	}

	public Object getParent(Object element) {
		if(element instanceof AccessPointEntity)
			return ((AccessPointEntity)element).getESS();
		return null;
	}

	public boolean hasChildren(Object element) {
		if(element instanceof ExtendedServiceSetEntity) {
			return ((ExtendedServiceSetEntity)element).getAccessPoints().size() > 0;
		}
		
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return networks.toArray();
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(newInput == null) {
			networks.clear();
			return;
		}
		if(!(newInput instanceof ISpace)  || newInput == currentSpace)
			return;
		
		final ISpace space = (ISpace) newInput;
		networks.clear();
		space.addChangeListenerAndPopulate(spaceListener);
		currentSpace = space;
		
		this.viewer = viewer;
		
		
	}
	
	private IEventHandler createSpaceListener() {
		return new IEventHandler() {
			public void handleEvent(IEvent event) {
				if(event instanceof ISpaceContentChangeEvent) {
					handleSpaceChange((ISpaceContentChangeEvent) event);
				}				
			}
		};
	}
	
	private void handleSpaceChange(ISpaceContentChangeEvent event) {
		if(event.isCreationEvent() && event.getEntity() instanceof ExtendedServiceSetEntity) {
			networks.add((ExtendedServiceSetEntity) event.getEntity());
			refreshViewer();
		}
	}
	
	private void refreshViewer() {
		if(viewer == null)
			return;
		synchronized(viewer) {
		viewer.getControl().getDisplay().asyncExec(new Runnable() {

			public void run() {
				viewer.refresh();				
			}
			
		});
		
	}
	}

}
