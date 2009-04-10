package com.netifera.platform.ui.updater;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.netifera.platform.ui.UIPlugin;

public class ScrollLockAction extends Action {
	private static final String SCROLL_LOCK_IMAGE = "icons/scroll_lock.png";
	private TableUpdater updater = null;

	protected ScrollLockAction() {
	}

	public ScrollLockAction(TableUpdater updater) {
		super("&Scroll lock",AS_CHECK_BOX);
		this.updater = updater;
		/* image is not shown in AS_CHECK_BOX style*/
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(UIPlugin.PLUGIN_ID, SCROLL_LOCK_IMAGE));
	}

	public void run() {
		updater.setAutoScroll(!isChecked());
	}
}
