package com.netifera.platform.net.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;

public class PortSetEntity extends AbstractEntity {
	
	private static final long serialVersionUID = -1215356725844239989L;

	public final static String ENTITY_NAME = "portset";

	private String ports;
	private final String protocol;
	private final IEntityReference address;

	public PortSetEntity(IWorkspace workspace, InternetAddressEntity address, String protocol) {
		super(ENTITY_NAME, workspace, address.getRealmId());
		this.protocol = protocol;
		this.address = address.createReference();
		this.ports = "";
	}

	PortSetEntity() {
		this.address = null;
		this.ports = null;
		this.protocol = null;
	}
	
	public InternetAddressEntity getAddress() {
		return (InternetAddressEntity) referenceToEntity(address);
	}
	
	public String getProtocol() {
		return protocol;
	}
	
	public String getPorts() {
		return ports;
	}
	
	public void setPorts(String ports) {
		this.ports = ports;
	}
	
	private PortSetEntity(IWorkspace workspace, IEntityReference addressReference,
			long realmId, String protocol) {
		super(ENTITY_NAME, workspace, realmId);
		this.protocol = protocol;
		this.address = addressReference.createClone();		
	}
	
	protected void synchronizeEntity(AbstractEntity masterEntity) {
		PortSetEntity portset = (PortSetEntity) masterEntity;
		this.ports = portset.getPorts();
	}
	
	protected IEntity cloneEntity() {
		PortSetEntity clone = new PortSetEntity(getWorkspace(), address, getRealmId(), protocol);
		clone.ports = new String(ports);
		return clone;
	}

}
