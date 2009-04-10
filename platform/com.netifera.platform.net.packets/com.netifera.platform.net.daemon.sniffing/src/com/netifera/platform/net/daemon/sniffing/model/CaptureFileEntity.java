package com.netifera.platform.net.daemon.sniffing.model;

import java.util.Date;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IWorkspace;

public class CaptureFileEntity extends AbstractEntity {
	//	implements Comparable<CaptureFileEntity> {
	
	private static final long serialVersionUID = 6396470259329718968L;

	public final static String ENTITY_NAME = "sniffing.capture_file";

	private String path;
	private long timestamp;
	
	public static CaptureFileEntity create(IWorkspace workspace, long realm,
			long spaceId, String path) {
		CaptureFileEntity entity = new CaptureFileEntity(workspace, realm,
				path, new Date().getTime());
		
		entity.save();
		entity.addToSpace(spaceId);
		return entity;
	}
	
	public CaptureFileEntity(IWorkspace workspace, long realm, String path,
			long timestamp) {
		super(ENTITY_NAME, workspace, realm);
		this.path = path;
		this.timestamp = timestamp;
	}
	
	public String getPath() {
		return path;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	@Override
	protected void synchronizeEntity(AbstractEntity masterEntity) {
		path = ((CaptureFileEntity)masterEntity).path;
		timestamp = ((CaptureFileEntity)masterEntity).timestamp;
	}
	
	@Override
	protected IEntity cloneEntity() {
		return new CaptureFileEntity(getWorkspace(), getRealmId(), 
				path, timestamp);
	}

	@Override
	public boolean isRealmEntity() {
		return true;
	}

	/*
	public int compareTo(CaptureFileEntity other) {
		int res = path.compareTo(other.path);
		if (res != 0) {
			return res;
		}
		return (timestamp < other.timestamp ? -1
					: (timestamp == other.timestamp ? 0 : 1));
	}
	*/
}
