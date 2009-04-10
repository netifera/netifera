package com.netifera.platform.net.dns.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;

public abstract class DNSRecordEntity extends AbstractEntity {
	
	private static final long serialVersionUID = 3899444105954802077L;
	final protected IEntityReference domain;

	protected DNSRecordEntity(String typeName, IWorkspace workspace,
			long realmId, IEntityReference domain) {
		super(typeName, workspace, realmId);
		this.domain = domain.createClone();
	}
	
	DNSRecordEntity() {
		this.domain = null;
	}
	public final DomainEntity getDomain() {
		return (DomainEntity) referenceToEntity(domain);
	}
}
