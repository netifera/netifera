package com.netifera.platform.net.wifi.ui.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.netifera.platform.net.model.NetworkAddressEntity;
import com.netifera.platform.net.wifi.model.AccessPointEntity;
import com.netifera.platform.net.wifi.model.ExtendedServiceSetEntity;
import com.netifera.platform.net.wifi.model.WirelessStationEntity;
import com.netifera.platform.ui.images.ImageCache;

public class WirelessLabelProvider extends ColumnLabelProvider {

	private final static String PLUGIN_ID = "com.netifera.platform.net.wifi.ui";

	private final ImageCache images = new ImageCache(PLUGIN_ID);
	private static final String ESS_IMAGE = "icons/wifi.png";
	private static final String ESS_ENCRYPTED_IMAGE = "icons/wifi_encrypted.png";
	private static final String AP_IMAGE = "icons/accesspoint.png";
	private static final String STATION_IMAGE = "icons/wifi16.png";

	

	public Image getImage(Object element){ 
		if(element instanceof ExtendedServiceSetEntity) {
			if(((ExtendedServiceSetEntity)element).isEncrypted())
				return images.get(ESS_ENCRYPTED_IMAGE);
			else
				return images.get(ESS_IMAGE);
		} else if(element instanceof AccessPointEntity) {
			return images.get(AP_IMAGE);
		} else if(element instanceof WirelessStationEntity) {
			return images.get(STATION_IMAGE);
		}
		return null;
	}
	public String getText(Object element) {
		if(element instanceof ExtendedServiceSetEntity)
			return ((ExtendedServiceSetEntity)element).getName();
		else if(element instanceof AccessPointEntity) 
			return accessPointText((AccessPointEntity) element);
		else if(element instanceof WirelessStationEntity)
			return stationText((WirelessStationEntity) element);
		else 
			return "??";
	}
	
	private String accessPointText(AccessPointEntity ap) {
		return ap.getBSSID().toString();
	}
	
	private String stationText(WirelessStationEntity station) {
		NetworkAddressEntity networkAddress = station.getNetworkAddress();
		if(networkAddress != null) {
			return station.getAddress().toString() + "  (" + networkAddress.getAddressString() + ")";
		}
		return station.getAddress().toString();
	}

}
