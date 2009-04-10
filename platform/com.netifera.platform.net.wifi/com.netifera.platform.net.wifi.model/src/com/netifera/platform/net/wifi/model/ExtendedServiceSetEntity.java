package com.netifera.platform.net.wifi.model;

import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;

public class ExtendedServiceSetEntity extends AbstractEntity {

	
	private static final long serialVersionUID = 1L;
	public static final String ENTITY_TYPE = "wireless.ess";
	private final String name;
	private boolean isEncrypted;
	private List<IEntityReference> accessPoints = new ArrayList<IEntityReference>();
	private List<IEntityReference> stations = new ArrayList<IEntityReference>();
	
	public ExtendedServiceSetEntity(IWorkspace workspace, long realmId, String name) {
		super(ENTITY_TYPE, workspace, realmId);
		this.name = name;
		
	}
	
	ExtendedServiceSetEntity() {
		name = null;
	}
	
	public void addAccessPoint(AccessPointEntity ap) {
		accessPoints.add(ap.createReference());
	}
	
	public void addStation(WirelessStationEntity station) {
		stations.add(station.createReference());
	}
	
	public List<AccessPointEntity> getAccessPoints() {
		List<AccessPointEntity> answer = new ArrayList<AccessPointEntity>();
		for(IEntityReference ref : accessPoints)
			answer.add((AccessPointEntity) referenceToEntity(ref));
		return answer;
	}
	
	public List<WirelessStationEntity> getStations() {
		List<WirelessStationEntity> answer = new ArrayList<WirelessStationEntity>();
		for(IEntityReference ref : stations)
			answer.add((WirelessStationEntity) referenceToEntity(ref));
		return answer;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isEncrypted() {
		return isEncrypted;
	}
	
	public void setEncrypted(boolean value) {
		this.isEncrypted = value;
	}
	
	public boolean isRealmEntity() {
		return true;
	}
	@Override
	protected IEntity cloneEntity() {
		final ExtendedServiceSetEntity clone = new ExtendedServiceSetEntity(getWorkspace(), getRealmId(), name);
		// ...
		return clone;
	}
	
	protected String generateQueryKey() {
		return createQueryKey(getRealmId(), name);
	}
	
	public static String createQueryKey(long realmId, String name) {
		return ENTITY_TYPE + ":" + realmId + ":" + name;
	}

}
