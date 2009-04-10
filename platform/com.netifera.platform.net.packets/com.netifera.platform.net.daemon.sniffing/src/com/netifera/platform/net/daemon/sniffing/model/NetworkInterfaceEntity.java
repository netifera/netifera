package com.netifera.platform.net.daemon.sniffing.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IWorkspace;

public class NetworkInterfaceEntity extends AbstractEntity {
	//	implements Comparable<NetworkInterfaceEntity> {
	
	private static final long serialVersionUID = 8048547796055853589L;

	public final static String ENTITY_NAME = "sniffing.interface";

	private String name;
	
	public static NetworkInterfaceEntity create(IWorkspace workspace,
			long realm, long spaceId, String name) {
		NetworkInterfaceEntity entity = new NetworkInterfaceEntity(workspace, realm, name);
		entity.save();
		entity.addToSpace(spaceId);
		return entity;
	}
	
	public NetworkInterfaceEntity(IWorkspace workspace, long realm, String name) {
		super(ENTITY_NAME, workspace, realm);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	protected void synchronizeEntity(AbstractEntity masterEntity) {
		name = ((NetworkInterfaceEntity)masterEntity).name;
	}
	
	@Override
	protected IEntity cloneEntity() {
		return new NetworkInterfaceEntity(getWorkspace(), getRealmId(), name);
	}

	@Override
	public boolean isRealmEntity() {
		return true;
	}

	/*
	public int compareTo(NetworkInterfaceEntity other) {
		return name.compareTo(other.name);
	}
	*/
}
