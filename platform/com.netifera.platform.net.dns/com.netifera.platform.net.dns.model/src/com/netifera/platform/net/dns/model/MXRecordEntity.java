package com.netifera.platform.net.dns.model;


import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.net.model.ServiceEntity;

public class MXRecordEntity extends DNSRecordEntity {
	
	private static final long serialVersionUID = -60010876317109369L;

	final public static String ENTITY_TYPE = "dns.mx";
	
	final private String target;
	final private Integer priority;
	private IEntityReference service;

	public MXRecordEntity(IWorkspace workspace, long realmId, IEntityReference domain, String target, Integer priority, ServiceEntity service) {
		this(workspace, realmId, domain, target, priority);
		if (service != null) setService(service);
	}

	public MXRecordEntity(IWorkspace workspace, long realmId, IEntityReference domain, String target, Integer priority) {
		super(ENTITY_TYPE, workspace, realmId, domain);
		this.target = target;
		this.priority = priority;
	}
	
	MXRecordEntity() {
		target = null;
		priority = null;
	}
	public String getTarget() {
		return target;
	}
	
	public Integer getPriority() {
		return priority;
	}
	
	public void setService(ServiceEntity service) {
		this.service = service.createReference();
	}
	
	public ServiceEntity getService() {
		return (ServiceEntity) this.referenceToEntity(service);
	}

	@Override
	protected void synchronizeEntity(AbstractEntity masterEntity) {
		this.service = ((MXRecordEntity)masterEntity).service;
	}

	@Override
	protected IEntity cloneEntity() {
		return new MXRecordEntity(getWorkspace(), getRealmId(), domain, target, priority, getService());
	}
	
	public static String createQueryKey(long realmId, String target, long domainId) {
		return ENTITY_TYPE + ":" + realmId + ":" + target + ":" + domainId;
	}
	
	@Override
	protected String generateQueryKey() {
		return createQueryKey(getRealmId(), target, getDomain().getId());
	}
}
