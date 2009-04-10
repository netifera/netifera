package com.netifera.platform.ui.spaces.tree;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.ui.api.model.IEntityLabelProviderService;
import com.netifera.platform.ui.internal.spaces.Activator;


public class SpaceTreeLabelProvider extends ColumnLabelProvider {
	private final IEntityLabelProviderService modelLabelProvider;
	
	public SpaceTreeLabelProvider() {
		modelLabelProvider = Activator.getDefault().getLabelProvider();
	}
	
	@Override
	public String getText(Object element) {
		if(element instanceof IShadowEntity) {
			return modelLabelProvider.getText((IShadowEntity)element);
		}
		return null;
	}
	
	@Override
	public Image getImage(Object element) {
		if(element instanceof IShadowEntity) {
			return modelLabelProvider.getImage((IShadowEntity)element);
		}
		return null;
	}
	

}
