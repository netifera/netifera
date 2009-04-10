package com.netifera.platform.ui.spaces.tree;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.ui.internal.spaces.Activator;

public class TreeViewerComparator extends ViewerComparator {
	
	public int category(Object element) {
		if(!(element instanceof IShadowEntity)) 
			return 0;
		
		return Activator.getDefault().getLabelProvider().getSortingCategory((IShadowEntity) element);
	}
	
	public int compare(Viewer viewer, Object e1, Object e2) {
		if(!(e1 instanceof IShadowEntity || e2 instanceof IShadowEntity))
			return super.compare(viewer, e1, e2);
		
		Integer result = Activator.getDefault().getLabelProvider().compare((IShadowEntity)e1, (IShadowEntity) e2);
		if (result == null)
			return super.compare(viewer, e1, e2);
		return result;
	}
}
