package com.netifera.platform.net.wifi.internal.model;

import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.model.IWorkspaceEx;
import com.netifera.platform.net.wifi.model.AccessPointEntity;
import com.netifera.platform.net.wifi.model.ExtendedServiceSetEntity;
import com.netifera.platform.net.wifi.model.IWirelessEntityFactory;
import com.netifera.platform.net.wifi.model.WirelessStationEntity;
import com.netifera.platform.util.addresses.MACAddress;

public class WirelessEntityFactory implements IWirelessEntityFactory {
	
	private static final String UNKNOWN_ESS_NAME = "<UNKNOWN>";
	
	private IModelService model;
	
	public ExtendedServiceSetEntity createExtendedServiceSet(long realmId, long spaceId, String name) {
		ExtendedServiceSetEntity ess = (ExtendedServiceSetEntity) getWorkspace().findByKey(ExtendedServiceSetEntity.createQueryKey(realmId, name));
		if(ess != null) {
			ess.addToSpace(spaceId);
			return ess;
		}
		
		ess = new ExtendedServiceSetEntity(getWorkspace(), realmId, name);
		ess.save();
		ess.addToSpace(spaceId);
		return ess;
	}
	

	public AccessPointEntity createAccessPoint(long realmId, long spaceId, MACAddress bssid, ExtendedServiceSetEntity ess) {
		AccessPointEntity ap = (AccessPointEntity) getWorkspace().findByKey(AccessPointEntity.createQueryKey(realmId, bssid));
		if(ap != null) {
			ap.addToSpace(spaceId);
			return ap;
		}
		if(ess == null) {
			ess = createExtendedServiceSet(realmId, spaceId, UNKNOWN_ESS_NAME);
		}
		
		ap = new AccessPointEntity(getWorkspace(), realmId, bssid, ess);
		ap.save();
		ap.addToSpace(spaceId);
		ess.addAccessPoint(ap);
		return ap;
	}
	
	public WirelessStationEntity createStation(long realmId, long spaceId, MACAddress address, ExtendedServiceSetEntity ess) {
		WirelessStationEntity station = (WirelessStationEntity) getWorkspace().findByKey(WirelessStationEntity.createQueryKey(realmId, address));
		if(station != null) {
			station.addToSpace(spaceId);
			return station;
		}
		if(ess == null)
			ess = createExtendedServiceSet(realmId, spaceId, UNKNOWN_ESS_NAME);
		
		station = new WirelessStationEntity(getWorkspace(), realmId, address, ess);
		station.save();
		station.addToSpace(spaceId);
		ess.addStation(station);
		return station;
	}
	protected void setModelService(IModelService model) {
		this.model = model;
	}
	
	protected void unsetModelService(IModelService model) {
		
	}
	
	private IWorkspaceEx getWorkspace() {
		if(model.getCurrentWorkspace() == null)
			throw new IllegalStateException("Cannot create entities because no workspace is currently open");
		return (IWorkspaceEx) model.getCurrentWorkspace();
	}
}
