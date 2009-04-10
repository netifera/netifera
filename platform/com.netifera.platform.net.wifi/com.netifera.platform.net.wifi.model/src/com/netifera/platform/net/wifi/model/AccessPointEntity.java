package com.netifera.platform.net.wifi.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.util.addresses.MACAddress;

public class AccessPointEntity extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;
	public static final String ENTITY_TYPE = "wireless.access_point";
	private final byte[] bssidBytes;
	private final IEntityReference essEntity;

	public AccessPointEntity(IWorkspace workspace, long realmId, MACAddress bssid, ExtendedServiceSetEntity ess) {
		super(ENTITY_TYPE, workspace, realmId);
		bssidBytes = bssid.toBytes();
		essEntity = ess.createReference();
	}
	
	AccessPointEntity() {
		bssidBytes = null;
		essEntity = null;
	}
	
	public MACAddress getBSSID() {
		return new MACAddress(bssidBytes);
	}
	
	public ExtendedServiceSetEntity getESS() {
		return (ExtendedServiceSetEntity) referenceToEntity(essEntity);
	}
	
	@Override
	protected IEntity cloneEntity() {
		final AccessPointEntity clone = new AccessPointEntity(getWorkspace(), getRealmId(), new MACAddress(bssidBytes), getESS());
		return clone;
	}

	protected String generateQueryKey() {
		return createQueryKey(getRealmId(), new MACAddress(bssidBytes));
	}
	public static String createQueryKey(long realmId, MACAddress bssid) {
		return ENTITY_TYPE + ":" + realmId + ":" + bssid.toString();
	}

}
