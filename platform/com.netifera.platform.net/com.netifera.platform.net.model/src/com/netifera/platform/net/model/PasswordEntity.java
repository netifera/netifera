package com.netifera.platform.net.model;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IWorkspace;

public class PasswordEntity extends CredentialEntity implements Comparable<PasswordEntity> {
	
	private static final long serialVersionUID = 1180178163608855982L;

	public final static String ENTITY_NAME = "password";

	private final String password;

	public PasswordEntity(IWorkspace workspace, IEntity authenticable, String password) {
		super(ENTITY_NAME, workspace, authenticable);
		this.password = password;
	}

	PasswordEntity() {
		password  = null;
	}
	
	public String getPassword() {
		return password;
	}
	
	protected IEntity cloneEntity() {
		return new PasswordEntity(getWorkspace(), getAuthenticable(), password);
	}
	public static String createQueryKey(long realmId, long authenticableId, String password) {
		return ENTITY_NAME + ":" + realmId + ":" + authenticableId + ":" + password;
	}
	
	@Override
	protected String generateQueryKey() {
		return createQueryKey(getRealmId(), getAuthenticable().getId(), password);
	}
	
	public int compareTo(PasswordEntity other) {
		int r = password.compareTo(other.password);
		return r > 0 ? 1 : (r < 0 ? -1 : 0);
	}
}

