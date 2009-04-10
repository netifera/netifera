package com.netifera.platform.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;

public class ProbeEntity extends AbstractEntity {
	
	private static final long serialVersionUID = -5545356433526823936L;
	public final static String ENTITY_NAME = "probe";
	private String name;
	private String channelConfig;
	private int probeId;
	private int lastUpdateIndex;
	private boolean local;
	private final IEntityReference hostEntity;
	
	
	public ProbeEntity(IWorkspace workspace, IEntity hostEntity) {
		super(ENTITY_NAME, workspace, hostEntity.getRealmId());
		lastUpdateIndex = 0;
		this.hostEntity = hostEntity.createReference();
	}
	public ProbeEntity(IWorkspace workspace, final long realmId) {
		super(ENTITY_NAME, workspace, realmId);
		lastUpdateIndex = 0;
		hostEntity = null;
	}
	
	public IEntity getHostEntity() {
		if(hostEntity == null)
			return null;
		else
			return referenceToEntity(hostEntity);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setChannelConfig(String config) {
		this.channelConfig = config;
	}
	
	public String getChannelConfig() {
		return channelConfig;
	}
	
	public int getProbeId() {
		return probeId;
	}
	public void setProbeId(int id) {
		this.probeId = id;
	}

	public int getUpdateIndex() {
		return lastUpdateIndex;
	}
	
	public void incrementUpdateIndex(int count) {
		lastUpdateIndex += count;
	}
	public boolean isLocal() {
		return local;
	}
	public void setLocal(final boolean local) {
		this.local = local;
	}

	protected IEntity cloneEntity() {
		ProbeEntity clone = new ProbeEntity(getWorkspace(), getRealmId());
		clone.name = name;
		clone.probeId = probeId;
		clone.local = local;
		return clone;
	}


	public boolean isRealmEntity() {
		return true;
	}
	
	
}
