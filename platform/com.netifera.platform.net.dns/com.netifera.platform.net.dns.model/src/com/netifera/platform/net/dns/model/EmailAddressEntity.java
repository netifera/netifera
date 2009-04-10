package com.netifera.platform.net.dns.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;


public class EmailAddressEntity extends AbstractEntity {
	
	private static final long serialVersionUID = 8749273022330944283L;

	final public static String ENTITY_TYPE = "address.email";

	final private String address;
	final private IEntityReference domain;
	
	private String name; // should be a PersonEntity
	
	public EmailAddressEntity(IWorkspace workspace, long realmId, String address, IEntityReference domainReference) {
		super(ENTITY_TYPE, workspace, realmId);
		this.address = address;
		this.domain = domainReference.createClone();
	}
	
	EmailAddressEntity() {
		this.address = null;
		this.domain = null;
	}
	
	public String getAddress() {
		return address;
	}
	
	public DomainEntity getDomain() {
		return (DomainEntity) referenceToEntity(domain);
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	@Override
	protected void synchronizeEntity(AbstractEntity masterEntity) {
		EmailAddressEntity entity = (EmailAddressEntity) masterEntity;
		name = entity.name;
	}

	@Override
	protected IEntity cloneEntity() {
		EmailAddressEntity answer = new EmailAddressEntity(getWorkspace(), getRealmId(), address, domain);
		answer.name = name;
		return answer;
	}
	
	public static String createQueryKey(long realmId, String address) {
		return ENTITY_TYPE + ":" + realmId + ":" + address;
	}
	
	@Override
	protected String generateQueryKey() {
		return createQueryKey(getRealmId(), address);
	}
}
