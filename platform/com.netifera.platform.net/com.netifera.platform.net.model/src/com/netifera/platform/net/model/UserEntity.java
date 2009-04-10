package com.netifera.platform.net.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;

public class UserEntity extends AbstractEntity {
		//implements Comparable<UserEntity> {
	
	private static final long serialVersionUID = 1049344731407259618L;

	public final static String ENTITY_NAME = "user";

	private final IEntityReference host;
	private final String name;

	public UserEntity(IWorkspace workspace, HostEntity host, String name) {
		super(ENTITY_NAME, workspace, host.getRealmId());
		this.host = host.createReference();
		this.name = name;
	}

	UserEntity() {
		this.host = null;
		this.name = null;
	}
	public HostEntity getHost() {
		return (HostEntity) referenceToEntity(host);
	}

	public String getName() {
		return name;
	}

	@Override
	protected IEntity cloneEntity() {
		return new UserEntity(getWorkspace(), getHost(), getName());
	}
	
	public static String createQueryKey(long realmId, String name, long hostId) {
		return ENTITY_NAME + ":" + realmId + ":" + name + ":" + hostId;
	}
	
	@Override
	protected String generateQueryKey() {
		return createQueryKey(getRealmId(), name, getHost().getId());
	}
	
	public int compareTo(UserEntity other) {
		int r = name.compareTo(other.name);
		return r > 0 ? 1 : (r < 0 ? -1 : 0);
	}
}
