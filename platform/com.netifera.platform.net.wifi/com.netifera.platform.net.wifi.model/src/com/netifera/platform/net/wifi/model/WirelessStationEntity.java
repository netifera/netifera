package com.netifera.platform.net.wifi.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.net.model.NetworkAddressEntity;
import com.netifera.platform.util.addresses.MACAddress;

public class WirelessStationEntity extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;
	public static final String ENTITY_TYPE = "wireless.station";
	private final byte[] addressBytes;
	private final IEntityReference essEntity;
	private IEntityReference networkAddress;
	
	public WirelessStationEntity(IWorkspace workspace, long realmId, MACAddress address, ExtendedServiceSetEntity ess) {
		super(ENTITY_TYPE, workspace, realmId);
		addressBytes = address.toBytes();
		essEntity = ess.createReference();
	}
	
	WirelessStationEntity() {
		addressBytes = null;
		essEntity = null;
	}
	
	public MACAddress getAddress() {
		return new MACAddress(addressBytes);
	}

	public ExtendedServiceSetEntity getESS() {
		return (ExtendedServiceSetEntity) referenceToEntity(essEntity);
	}
	
	public void setNetworkAddress(NetworkAddressEntity address) {
		networkAddress = address.createReference();
	}
	
	public NetworkAddressEntity getNetworkAddress() {
		if(networkAddress == null) return null;
		return (NetworkAddressEntity) referenceToEntity(networkAddress);
	}
	
	@Override
	protected IEntity cloneEntity() {
		final WirelessStationEntity clone = new WirelessStationEntity(getWorkspace(), getRealmId(), new MACAddress(addressBytes), getESS());
		return clone;
	}
	
	protected String generateQueryKey() {
		return createQueryKey(getRealmId(), new MACAddress(addressBytes));
	}
	
	public static String createQueryKey(long realmId, MACAddress address) {
		return ENTITY_TYPE + ":" + realmId + ":" + address.toString();
	}

}
