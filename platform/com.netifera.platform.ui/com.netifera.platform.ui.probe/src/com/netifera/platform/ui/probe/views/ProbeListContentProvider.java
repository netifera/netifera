package com.netifera.platform.ui.probe.views;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.probe.IProbeManagerService;

public class ProbeListContentProvider implements IStructuredContentProvider {

	private IProbeManagerService probeManager;
	
	public Object[] getElements(Object inputElement) {
		List<IProbe> probes = probeManager.getProbeList();
		Collections.sort(probes, new Comparator<IProbe>() {

			public int compare(IProbe p1, IProbe p2) {
				
				if(p1.isLocalProbe()) {
					return -1;
				}
				if(p2.isLocalProbe()) {
					return 1;
				}
				if(p1.isConnected() && !p2.isConnected()) {
					return -1;
				}
				if(p2.isConnected() && !p1.isConnected()) {
					return 1;
				}
				return p1.getName().compareTo(p2.getName());
			}
			
		});
		return probes.toArray();
	}

	public void dispose() {		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(newInput instanceof IProbeManagerService) {
			probeManager = (IProbeManagerService) newInput;
		}
	}

}
