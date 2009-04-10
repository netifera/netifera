/**
 * 
 */
package com.netifera.platform.ui.util;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.ui.UIPlugin;

/**
 * @author kevin
 * 
 */
public class TreeAction extends Action {
	public final static String EXPAND_ID = "com.netifera.platform.ui.action.expandtree";
	public final static String COLLAPSE_ID = "com.netifera.platform.ui.action.collapsetree";
	private static final String TREE_EXPAND_IMAGE = "icons/expandall.png";
	private static final String TREE_COLLAPSE_IMAGE = "icons/collapseall.png";
	
	private AbstractTreeViewer treeViewer;
	private boolean expand;

	public static TreeAction collapseAll(AbstractTreeViewer treeViewer) {
		return new TreeAction(treeViewer, false);
	}

	public static TreeAction expandAll(AbstractTreeViewer treeViewer) {
		return new TreeAction(treeViewer, true);
	}

	public TreeAction(AbstractTreeViewer treeViewer, boolean expand) {
		this.treeViewer = treeViewer;
		this.expand = expand;
		setId( expand ? (EXPAND_ID) : (COLLAPSE_ID) );
		setLabels();
	}

	private void setLabels() {
		setText(expand ? "Expand All" : "Collapse All");
		String imageKey = expand ? TREE_EXPAND_IMAGE : TREE_COLLAPSE_IMAGE;
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(UIPlugin.PLUGIN_ID, imageKey)); 
	}

	public void run() {
		if (expand) {
			treeViewer.expandAll();
		} else {
			treeViewer.collapseAll();
		}
	}
	
	
}
