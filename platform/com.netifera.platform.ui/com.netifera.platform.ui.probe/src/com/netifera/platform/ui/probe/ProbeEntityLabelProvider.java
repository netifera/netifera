package com.netifera.platform.ui.probe;

import org.eclipse.swt.graphics.Image;

import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.model.ProbeEntity;
import com.netifera.platform.ui.api.model.IEntityLabelProvider;
import com.netifera.platform.ui.images.ImageCache;

public class ProbeEntityLabelProvider implements IEntityLabelProvider {
	
	private final static String PLUGIN_ID = "com.netifera.platform.ui.probe";
	
	private ImageCache images = new ImageCache(PLUGIN_ID);
	private final static String PROBE_CONNECTED = "icons/probe_connected.png";
	private final static String PROBE_CONNECTING = "icons/probe_connecting.png";
	private final static String PROBE_DISCONNECTED = "icons/probe_disconnected.png";
	private final static String PROBE_FAILED = "icons/probe_failed.png";

	public String getText(IShadowEntity e) {
		if (e instanceof ProbeEntity) {
			return "Remote Probe";
		}
		return null;
	}

	public String getFullText(IShadowEntity e) {
		return getText(e);
	}

	public Image getImage(IShadowEntity e) {
		if(e instanceof ProbeEntity) {
			return getProbeImage((ProbeEntity) e);
		}
		return null;
	}
	
	public Image decorateImage(Image image, IShadowEntity e) {
		return null;
	}

	private Image getProbeImage(ProbeEntity probeEntity) {
		IProbeManagerService probeManager = Activator.getDefault().getProbeManager();
		IProbe probe = probeManager.getProbeById(probeEntity.getProbeId());
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
	
	public void dispose() {
		images.dispose();		
	}

	public Integer getSortingCategory(IShadowEntity e) {
		return null;
	}

	public Integer compare(IShadowEntity e1, IShadowEntity e2) {
		return null;
	}

}
