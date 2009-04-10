package com.netifera.platform.net.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;

public abstract class CredentialEntity extends AbstractEntity {
	
	private static final long serialVersionUID = -5113289303307574052L;
	private final IEntityReference authenticable;

	public CredentialEntity(String entityName, IWorkspace workspace, IEntity authenticable) {
		super(entityName, workspace, authenticable.getRealmId());
		this.authenticable = authenticable.createReference();
	}

	CredentialEntity() {
		authenticable = null;
	}
	public IEntity getAuthenticable() {
		return referenceToEntity(authenticable);
	}
}
