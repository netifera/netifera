package com.netifera.platform.net.wifi.ui.labels;

import org.eclipse.swt.graphics.Image;

import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.net.wifi.model.AccessPointEntity;
import com.netifera.platform.net.wifi.model.ExtendedServiceSetEntity;
import com.netifera.platform.ui.api.model.IEntityLabelProvider;
import com.netifera.platform.ui.images.ImageCache;
import com.netifera.platform.util.addresses.MACAddress;

public class WirelessLabelProvider implements IEntityLabelProvider {
	
	private final static String PLUGIN_ID = "com.netifera.platform.net.wifi.ui";

	private final ImageCache images = new ImageCache(PLUGIN_ID);
	private static final String ESS_IMAGE = "icons/wifi.png";
	private static final String AP_IMAGE = "icons/accesspoint.png";
	
	public String getFullText(IShadowEntity e) {
		return getText(e);
	}

	public Image getImage(IShadowEntity e) {
		if(e instanceof ExtendedServiceSetEntity) {
			return images.get(ESS_IMAGE);
		} else if(e instanceof AccessPointEntity) {
			return images.get(AP_IMAGE);
		}
		else {
			return null;
		}
	}

	public Image decorateImage(Image image, IShadowEntity e) {
		return null;
	}
	
	public String getText(IShadowEntity e) {
		if(e instanceof ExtendedServiceSetEntity) {
			return ((ExtendedServiceSetEntity)e).getName();
		} else if(e instanceof AccessPointEntity) {
			return getAccessPointText((AccessPointEntity) e);
		}
		else {
			return null;
		}
	}

	private String getAccessPointText(AccessPointEntity ap) {
		ExtendedServiceSetEntity ess = ap.getESS();
		MACAddress bssid = ap.getBSSID();
		return ess.getName() + "  [" + bssid.toString() + "]";
	}
	
	public Integer getSortingCategory(IShadowEntity e) {
		if(e instanceof AccessPointEntity)
			return 0;
		
		return null;
	}

	public Integer compare(IShadowEntity e1, IShadowEntity e2) {
		if(e1 instanceof ExtendedServiceSetEntity && e2 instanceof ExtendedServiceSetEntity) {
			return ((ExtendedServiceSetEntity)e1).getName().compareToIgnoreCase(
					((ExtendedServiceSetEntity)e2).getName());
		}
		return null;
	}

	public void dispose() {
		images.dispose();
	}
}
