package com.netifera.platform.net.wifi.model;

import com.netifera.platform.util.addresses.MACAddress;

public interface IWirelessEntityFactory {
	ExtendedServiceSetEntity createExtendedServiceSet(long realmId, long spaceId, String name);
	AccessPointEntity createAccessPoint(long realmId, long spaceId, MACAddress bssid, ExtendedServiceSetEntity ess);
	WirelessStationEntity createStation(long realmId, long spaceId, MACAddress address, ExtendedServiceSetEntity ess);
}
