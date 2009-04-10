/**
 * 
 */
package com.netifera.platform.ui.util;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.ui.UIPlugin;

/**
 * @author kevin
 *
 */
public class ViewerRefreshAction extends Action {
	private static final String REFRESH_IMAGE = "icons/refresh.png";
	protected Viewer viewer;

	public ViewerRefreshAction(){
		this(null);
	}

	public ViewerRefreshAction(Viewer viewer) {
		this.viewer = viewer;
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(UIPlugin.PLUGIN_ID, REFRESH_IMAGE));
		setText("Refresh");
	}
	
	public void setViewer(Viewer viewer) {
		this.viewer = viewer;
	}
	
	public void run() {
		if(viewer != null && !viewer.getControl().isDisposed()) {
			viewer.getControl().setRedraw(false);
			viewer.refresh();
			viewer.getControl().setRedraw(true);
		}
	}
}
