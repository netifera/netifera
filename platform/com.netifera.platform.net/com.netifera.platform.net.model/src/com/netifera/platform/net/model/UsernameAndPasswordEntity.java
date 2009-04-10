package com.netifera.platform.net.model;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IWorkspace;

public class UsernameAndPasswordEntity extends CredentialEntity implements Comparable<UsernameAndPasswordEntity> {
	
	private static final long serialVersionUID = 1254054940539549564L;

	public final static String ENTITY_NAME = "usernameandpassword";

	private final String username;
	private final String password;

	public UsernameAndPasswordEntity(IWorkspace workspace, IEntity authenticable, String username, String password) {
		super(ENTITY_NAME, workspace, authenticable);
		this.username = username;
		this.password = password;
	}

	UsernameAndPasswordEntity() {
		username = null;
		password = null;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	protected IEntity cloneEntity() {
		return new UsernameAndPasswordEntity(getWorkspace(), getAuthenticable(), username, password);
	}
	
	public static String createQueryKey(long realmId, long authenticableId, String username, String password) {
		return ENTITY_NAME + ":" + realmId + ":" + authenticableId + ":" + username + ":" + password;
	}
	
	@Override
	protected String generateQueryKey() {
		return createQueryKey(getRealmId(), getAuthenticable().getId(), username, password);
	}
	
	public int compareTo(UsernameAndPasswordEntity other) {
		int r = username.compareTo(other.username);
		if (r != 0) {
			return r > 0 ? 1 : -1;
		}
		r = password.compareTo(other.password);
		return r > 0 ? 1 : (r < 0 ? -1 : 0);
	}
}
