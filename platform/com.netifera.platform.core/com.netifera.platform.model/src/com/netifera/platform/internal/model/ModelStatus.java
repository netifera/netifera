package com.netifera.platform.internal.model;

import java.util.List;

import com.db4o.ObjectContainer;

public class ModelStatus {
	private final static int FIRST_PROBE_ID = 0x1000;
	private long modelVersion;
	private long currentEntityId;
	private int currentProbeId;
	private long currentTaskId;
	private long idPrefix;
	private transient ObjectContainer db;
	
	public static ModelStatus getModelStatus(final ObjectContainer db) {
		ModelStatus status;
		List<ModelStatus> result = db.query(ModelStatus.class);
		if(result.size() == 0) {
			status = new ModelStatus(db);
			status.currentProbeId = FIRST_PROBE_ID - 1;
			status.modelVersion = Workspace.MODEL_VERSION;
			db.store(status);
		} else {
			status = result.get(0);
			status.db = db;
		}
		return status;
	}
	

	private ModelStatus(final ObjectContainer db) {
		this.db = db;
	}
	
	synchronized boolean setIdPrefix(long prefix, boolean force) {
		if(prefix == idPrefix) 
			return true;
		
		if(idPrefix == 0 || force) {
			idPrefix = prefix;
			db.store(this);
			return true;
		} else {
			return false;
		}
	
	}
	
	long getIdPrefix() {
		return idPrefix;
	}
	
	long getModelVersion() {
		return modelVersion;
	}
	synchronized long generateEntityId() {
		currentEntityId += 1;
		db.store(this);
		return (idPrefix << 32) | currentEntityId;
	}
	
	synchronized int generateProbeId() {
		currentProbeId += 1;
		db.store(this);
		return currentProbeId;
	}
	
	synchronized long generateTaskId() {
		currentTaskId += 1;
		db.store(this);
		return (idPrefix << 32) | currentTaskId;
	}

}
