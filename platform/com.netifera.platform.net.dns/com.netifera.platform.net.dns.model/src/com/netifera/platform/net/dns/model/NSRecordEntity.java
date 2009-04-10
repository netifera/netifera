package com.netifera.platform.net.dns.model;


import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.net.model.ServiceEntity;

public class NSRecordEntity extends DNSRecordEntity {
	
	private static final long serialVersionUID = 3935582357663185104L;

	final public static String ENTITY_TYPE = "dns.ns";

	final private String target;
	private IEntityReference service;
	
	public NSRecordEntity(IWorkspace workspace, long realmId, IEntityReference domain, String target, ServiceEntity service) {
		this(workspace, realmId, domain, target);
		if (service != null) setService(service);
	}
	
	public NSRecordEntity(IWorkspace workspace, long realmId, IEntityReference domain, String target) {
		super(ENTITY_TYPE, workspace, realmId, domain);
		this.target = target;
	}
	
	NSRecordEntity() {
		target = null;
		service = null;
	}
	public String getTarget() {
		return target;
	}

	public void setService(ServiceEntity service) {
		this.service = service.createReference();
	}
	
	public ServiceEntity getService() {
		return (ServiceEntity) this.referenceToEntity(service);
	}
	
	@Override
	protected void synchronizeEntity(AbstractEntity masterEntity) {
		this.service = ((NSRecordEntity)masterEntity).service;
	}

	@Override
	protected IEntity cloneEntity() {
		return new NSRecordEntity(getWorkspace(), getRealmId(), domain, target, getService());
	}
	
	public static String createQueryKey(long realmId, String target, long domainId) {
		return ENTITY_TYPE + ":" + realmId + ":" + target + ":" + domainId;
	}
	
	@Override
	protected String generateQueryKey() {
		return createQueryKey(getRealmId(), target, getDomain().getId());
	}
}
