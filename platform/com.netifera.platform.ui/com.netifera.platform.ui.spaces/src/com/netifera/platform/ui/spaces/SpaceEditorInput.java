package com.netifera.platform.ui.spaces;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.ui.internal.spaces.Activator;

public class SpaceEditorInput implements IEditorInput {
	public final static String ID = "com.netifera.platform.editors.spaces";

	private final ISpace space;
	private final IProbe probe;
	
	public SpaceEditorInput(ISpace space) {
		this.space = space;
		this.probe = findProbeForSpace(space);
	}
	
	private IProbe findProbeForSpace(ISpace space) {
		final IProbeManagerService probeManager = Activator.getDefault().getProbeManager();
		if(probeManager == null) {
			throw new RuntimeException("Cannot create editor because probe manager service is not available");
		}
		final IProbe probe = probeManager.getProbeById(space.getProbeId());
		if(probe == null) {
			throw new RuntimeException("Cannot create editor because probe associated with space does not exist.");
		}
		return probe;
	}
	
	public ISpace getSpace() {
		return space;
	}
	
	public IProbe getProbeForSpace() {
		return probe;
	}
	
	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return space.getName();
	}

	public IPersistableElement getPersistable() {
		return new IPersistableElement() {

			public String getFactoryId() {
				return ElementFactory.ID;
			}

			public void saveState(IMemento memento) {
				memento.putInteger("space-id", (int) space.getId());				
			}
			
		};
	}

	public String getToolTipText() {
		return "Space:  " + space.getName();
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof SpaceEditorInput)) {
			return false;
		}
		return ((SpaceEditorInput)o).space.getId() == space.getId();
	}
	
	@Override
	public int hashCode() {
		return (int) (probe.getProbeId() ^ space.getId()); 
	}
	
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

}
