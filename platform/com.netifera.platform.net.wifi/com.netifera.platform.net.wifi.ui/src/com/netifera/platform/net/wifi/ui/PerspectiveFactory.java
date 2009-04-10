package com.netifera.platform.net.wifi.ui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.netifera.platform.net.wifi.ui.views.WirelessView;

public class PerspectiveFactory implements IPerspectiveFactory {
	public static final String ID = "com.netifera.platform.ui.perspectives.wifi";

	public void createInitialLayout(IPageLayout layout) {
		layout.addView(WirelessView.ID, IPageLayout.LEFT, 0.40f, layout.getEditorArea());
	}

}
