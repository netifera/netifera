package com.netifera.platform.internal.model;

import java.util.ArrayList;
import java.util.List;

import com.db4o.ObjectContainer;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.model.ProbeEntity;

public class UpdateTable {
	
	private final List<UpdateRecord> updateTable;
	private transient ObjectContainer db;
	private transient Workspace workspace;
	private transient boolean enabled = false;
	
	static UpdateTable getUpdateTable(ObjectContainer db, Workspace workspace) {
		UpdateTable table;
		List<UpdateTable> result = db.query(UpdateTable.class);
		if(result.size() == 0) {
			table = new UpdateTable(db, workspace);
			db.store(table);
			table.setEnabled();
		} else {
			table = result.get(0);
			table.db = db;
			table.workspace = workspace;
			table.setEnabled();
		}
		return table;
	}
	
	UpdateTable(ObjectContainer db, Workspace workspace) {
		updateTable = new ArrayList<UpdateRecord>();
		this.db = db;
		this.workspace = workspace;
	}
	
	private void setEnabled() {
		enabled = System.getProperty("netifera.updatetable") != null;
	}
	synchronized void updateEntity(IEntity entity) {
		if(enabled) {
			if(entity instanceof ProbeEntity) {
				return;
			}
			
			long idx = updateTable.size();
			updateTable.add(UpdateRecord.create(workspace, entity, idx));
			//db.store(this);
		}
	}
	
	synchronized void addEntityToSpace(IEntity entity, long spaceId) {
		if(enabled) {
			long idx = updateTable.size();
			updateTable.add(UpdateRecord.createAddedToSpace(workspace, entity, spaceId, idx));
			//db.store(this);
		}
	}
	synchronized long getCurrentUpdateIndex() {
		return updateTable.size();
	}
	
	synchronized UpdateRecord getUpdateElement(int index) {
		if(index < 0 || index >= updateTable.size()) {
			throw new IllegalArgumentException();
		}
		return updateTable.get(index);
	}

}
