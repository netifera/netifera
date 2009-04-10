package com.netifera.platform.ui.spaces;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.ui.internal.spaces.Activator;

public class ElementFactory implements IElementFactory {
	public final static String ID = "com.netifera.platform.spaceElementFactory";

	public IAdaptable createElement(IMemento memento) {
		
		final Integer spaceId = memento.getInteger("space-id");

		if(spaceId == null) 
			return null;

		ISpace space = createSpaceFromId(spaceId);
		if(space == null)
			return null;
		space.open();
		return new SpaceEditorInput(space);
	}
	
	private ISpace createSpaceFromId(long id) {
		IModelService model = Activator.getDefault().getModel();
		if(model == null) return null;
		return model.getCurrentWorkspace().findSpaceById(id);
	}
}
