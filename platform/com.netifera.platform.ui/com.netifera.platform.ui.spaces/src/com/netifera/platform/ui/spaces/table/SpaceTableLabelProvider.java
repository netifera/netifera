package com.netifera.platform.ui.spaces.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.ui.api.model.IEntityLabelProviderService;
import com.netifera.platform.ui.internal.spaces.Activator;


public class SpaceTableLabelProvider extends LabelProvider implements ITableLabelProvider {
	private final IEntityLabelProviderService entityLabelProvider;
	
	public SpaceTableLabelProvider() {
		entityLabelProvider = Activator.getDefault().getLabelProvider();
	}
	
	@Override
	public String getText(Object element) {
		if(element instanceof IShadowEntity) {
			return entityLabelProvider.getFullText((IShadowEntity)element);
		}
		return null;
	}
	
	@Override
	public Image getImage(Object element) {
		if(element instanceof IShadowEntity) {
			return entityLabelProvider.getImage((IShadowEntity)element);
		}
		return null;
	}

	public Image getColumnImage(Object element, int column) {
		if (column == 0)
			return getImage(element);
		return null;
	}

	public String getColumnText(Object element, int column) {
		if (column == 0)
			return getText(element);
		if (column == 1) {
			StringBuffer buffer = new StringBuffer();
			List<String> tags = new ArrayList<String>();
			tags.addAll(((IEntity)element).getTags());
			Collections.sort(tags);
			for (int i=0; i<tags.size(); i++) {
				if (i > 0)
					buffer.append(", ");
				buffer.append(tags.get(i));
			}
			return buffer.toString();
		}
		if (column == 2)
			return element.getClass().getSimpleName();
		return null;
	}
}
