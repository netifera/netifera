package com.netifera.platform.ui.spaces.views;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.ui.images.ImageCache;
import com.netifera.platform.ui.internal.spaces.Activator;

public class SpaceListLabelProvider extends LabelProvider implements ITableLabelProvider {

	private final static String OPEN_FOLDER = "icons/open.gif";
	private final static String CLOSED_FOLDER = "icons/closed.gif";
	private final static String PROBE_DISCONNECTED = "icons/probe_disconnected.png";
	private final static String PROBE_CONNECTING = "icons/probe_connecting.png";
	private final static String PROBE_CONNECTED = "icons/probe_connected.png";
	private final static String PROBE_FAILED = "icons/probe_failed.png";
	public static final String PROBE_PLUGIN_ID = "com.netifera.platform.ui.probe";

	
	private final ImageCache images = new ImageCache(Activator.PLUGIN_ID);
	private final ImageCache probeImages = new ImageCache(PROBE_PLUGIN_ID);
	
	public Image getColumnImage(Object element, int columnIndex) {
		final ISpace space = (ISpace) element;

		switch(columnIndex) {
		case 0:
			if(space.isOpened()) {
				return images.get(OPEN_FOLDER);
			} else {
				return images.get(CLOSED_FOLDER);
			}
		case 1:
			return getProbeStatusImage(space);
		
		default:
				return null;
		}
		
	}

	private Image getProbeStatusImage(ISpace space) {
		IProbe probe = getProbe(space);
		switch(probe.getConnectState()) {
		case CONNECTED:
			return probeImages.get(PROBE_CONNECTED);
		case CONNECTING:
			return probeImages.get(PROBE_CONNECTING);
		case DISCONNECTED:
			return probeImages.get(PROBE_DISCONNECTED);
		case CONNECT_FAILED:
			return probeImages.get(PROBE_FAILED);
		
		default:
			return null;
		}
		
	}
	
	public String getColumnText(Object element, int columnIndex) {
		if(!(element instanceof ISpace)) {
			return "??";
		}
		
		final ISpace space = (ISpace) element;
		switch(columnIndex) {
		case 0:
			return space.getName() + "  (" + space.entityCount() + ")";
		case 1:
			IProbe probe = getProbe(space);
			return probe.getName();
		default:
			return null;
			
		}
	}

	private IProbe getProbe(ISpace space) {
		return Activator.getDefault().getProbeManager().getProbeById(space.getProbeId());
	}
}
