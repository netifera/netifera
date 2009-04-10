package com.netifera.platform.net.dns.model;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;

public class ARecordEntity extends AddressRecordEntity {
	
	private static final long serialVersionUID = 674462864439975874L;
	
	public static final String ENTITY_TYPE = "dns.a";
	
	public ARecordEntity(IWorkspace workspace, long realmId, IEntityReference domain, String hostname, IEntityReference address) {
		super(ENTITY_TYPE, workspace, realmId, domain, hostname, address);
	}
	
	ARecordEntity() {
		// Do not remove this
	}
	
	@Override
	protected IEntity cloneEntity() {
		return new ARecordEntity(getWorkspace(), getRealmId(), domain, getHostName(), address);
	}
	
	public static String createQueryKey(long realmId, String address, String fqdm) {
		return AddressRecordEntity.createQueryKey(ENTITY_TYPE, realmId, address, fqdm);
	}
}
