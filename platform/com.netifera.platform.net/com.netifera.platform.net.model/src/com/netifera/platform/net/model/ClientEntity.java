package com.netifera.platform.net.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;

public class ClientEntity extends AbstractEntity {
	
	private static final long serialVersionUID = -1611903324378815778L;

	public final static String ENTITY_NAME = "client";
	
	private final IEntityReference host;
	private final String serviceType;
	
	public ClientEntity(IWorkspace workspace, HostEntity host, String serviceType) {
		super(ENTITY_NAME, workspace, host.getRealmId());
		this.host = host.createReference();
		this.serviceType = serviceType;
	}

	ClientEntity() {
		this.host = null;
		this.serviceType = null;
	}
	
	public HostEntity getHost() {
		return (HostEntity) referenceToEntity(host);
	}
	
	public String getServiceType() {
		return serviceType;
	}

	public String getBanner() {
		return getNamedAttribute("banner");
	}

	public String getProduct() {
		return getNamedAttribute("product");
	}
	
	public String getVersion() {
		return getNamedAttribute("version");
	}
	
	public void setBanner(String banner) {
		setNamedAttribute("banner", banner);
	}

	public void setProduct(String product) {
		setNamedAttribute("product", product);
	}

	public void setVersion(String version) {
		setNamedAttribute("version", version);
	}

	private ClientEntity(IWorkspace workspace, long realm, IEntityReference hostReference, String serviceType) {
		super(ENTITY_NAME, workspace, realm);
		this.host = hostReference.createClone();
		this.serviceType = serviceType;
	}
	
	protected IEntity cloneEntity() {
		return new ClientEntity(getWorkspace(), 
				getRealmId(), host, serviceType); 
	}
	
	public static String createQueryKey(long realmId, long hostId, String serviceType, String product) {
		String answer = ENTITY_NAME + ":" + realmId + ":" + hostId + ":" + serviceType;
		if (product != null)
			answer += ":" + product;
		return answer;
	}
	
	protected String generateQueryKey() {
		return createQueryKey(getRealmId(), getHost().getId(), serviceType, getProduct());
	}
}
