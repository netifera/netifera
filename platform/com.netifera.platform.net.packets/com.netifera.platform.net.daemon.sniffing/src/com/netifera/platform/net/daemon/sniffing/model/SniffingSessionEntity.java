package com.netifera.platform.net.daemon.sniffing.model;

import java.util.Date;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IWorkspace;


public class SniffingSessionEntity extends AbstractEntity {
		//implements Comparable<SniffingSessionEntity> {
	
	private static final long serialVersionUID = 2369468200195902794L;

	public final static String ENTITY_NAME = "sniffing.session";

	private long timestamp;

	public static SniffingSessionEntity create(IWorkspace workspace,
			long realm, long spaceId) {
		SniffingSessionEntity entity = new SniffingSessionEntity(
				workspace, realm,
				new Date().getTime());
		entity.save();
		entity.addToSpace(spaceId);
		return entity;
	}
	
	public SniffingSessionEntity(IWorkspace workspace,
			long realm, long timestamp) {
		super(ENTITY_NAME, workspace, realm);
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	@Override
	protected void synchronizeEntity(AbstractEntity masterEntity) {
		timestamp = ((SniffingSessionEntity)masterEntity).timestamp;
	}
	
	@Override
	protected IEntity cloneEntity() {
		return new SniffingSessionEntity(getWorkspace(), getRealmId(),
				timestamp);
	}
	
	@Override
	public boolean isRealmEntity() {
		return true;
	}

	/*
	public int compareTo(SniffingSessionEntity other) {
		return (timestamp < other.timestamp ? -1
				: (timestamp == other.timestamp ? 0 : 1));
	}
	*/
}
