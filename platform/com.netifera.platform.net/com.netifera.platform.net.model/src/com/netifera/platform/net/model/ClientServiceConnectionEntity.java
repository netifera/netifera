package com.netifera.platform.net.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;

public class ClientServiceConnectionEntity extends AbstractEntity {
	
	private static final long serialVersionUID = -7226430086566787932L;

	public final static String ENTITY_NAME = "service-connection";
	
	private final IEntityReference client;
	private final IEntityReference service;
	private final String identity;
	
	public ClientServiceConnectionEntity(IWorkspace workspace, ClientEntity client, ServiceEntity service, String identity) {
		super(ENTITY_NAME, workspace, client.getRealmId());
		this.client = client.createReference();
		this.service = service.createReference();
		this.identity = identity;
	}

	ClientServiceConnectionEntity() {
		client = null;
		service = null;
		identity = null;
	}
	
	public ClientEntity getClient() {
		return (ClientEntity) referenceToEntity(client);
	}

	public ServiceEntity getService() {
		return (ServiceEntity) referenceToEntity(service);
	}
	
	public String getIdentity() {
		return identity;
	}
	
	private ClientServiceConnectionEntity(IWorkspace workspace, long realm, IEntityReference clientReference, IEntityReference serviceReference, String identity) {
		super(ENTITY_NAME, workspace, realm);
		this.client = clientReference.createClone();
		this.service = serviceReference.createClone();
		this.identity = identity;
	}
	
	protected IEntity cloneEntity() {
		ClientServiceConnectionEntity clone =
			new ClientServiceConnectionEntity(getWorkspace(), getRealmId(),
					client, service, identity);
		return clone;
	}
	
	public static String createQueryKey(long realmId, long clientId, long serviceId, String identity) {
		return ENTITY_NAME + ":" + realmId + ":" + clientId + ":" + serviceId + ((identity == null) ? "" : (":" + identity));
	}
	
	protected String generateQueryKey() {
		return createQueryKey(getRealmId(), getClient().getId(), getService().getId(), identity);
	}
}
