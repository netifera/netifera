package com.netifera.platform.ui.probe.views;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.probe.IProbe.ConnectState;
import com.netifera.platform.ui.images.ImageCache;
import com.netifera.platform.ui.probe.Activator;

public class ProbeListLabelProvider extends LabelProvider implements ITableLabelProvider {

	//private final static String PROBE_IMAGE = "icons/probe.png";
	private final static String PROBE_DISCONNECTED = "icons/probe_disconnected.png";
	private final static String PROBE_CONNECTING = "icons/probe_connecting.png";
	private final static String PROBE_CONNECTED = "icons/probe_connected.png";
	private final static String PROBE_FAILED = "icons/probe_failed.png";

	private final ImageCache images = new ImageCache(Activator.PLUGIN_ID);
	
	public Image getColumnImage(Object element, int columnIndex) {
		if(columnIndex != 0) {
			return null;
		}
		if(!(element instanceof IProbe)) {
			return null;
		}
		final IProbe probe = (IProbe) element;
		switch(probe.getConnectState()) {
		case CONNECTED:
			return images.get(PROBE_CONNECTED);
		case CONNECTING:
			return images.get(PROBE_CONNECTING);
		case DISCONNECTED:
			return images.get(PROBE_DISCONNECTED);
		case CONNECT_FAILED:
			return images.get(PROBE_FAILED);
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if(!(element instanceof IProbe)) {
			return "??";
		}
		if(columnIndex != 1) {
			return null;
		}
		
		final IProbe probe = (IProbe) element;
		if(probe.getConnectState() == ConnectState.CONNECT_FAILED) {
			return probe.getName() + "  (" + probe.getConnectError() + ")";
		} else {
			return probe.getName();
		}
		
	}

}
