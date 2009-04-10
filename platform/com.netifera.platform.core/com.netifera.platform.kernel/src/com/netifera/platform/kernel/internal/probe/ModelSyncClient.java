package com.netifera.platform.kernel.internal.probe;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.model.IUpdateRecord;
import com.netifera.platform.model.ModelUpdate;
import com.netifera.platform.model.ProbeEntity;

public class ModelSyncClient {
	private final IWorkspace workspace;
	private final IProbe probe;
	private final ILogger logger;
	
	ModelSyncClient(IProbe probe, IWorkspace workspace, ILogger logger) {
		this.probe = probe;
		this.workspace = workspace;
		this.logger = logger;
	}
	void processModelUpdate(ModelUpdate updateMessage) {
		final ProbeEntity probeEntity = (ProbeEntity) probe.getEntity();
		for(IUpdateRecord update : updateMessage.getUpdateRecords()) {
			processUpdate(update);
			
			if(update.getUpdateIndex() != probeEntity.getUpdateIndex()) {
				logger.warning("Unexpected update index received: " + update.getUpdateIndex() + 
						" expecting " + probeEntity.getUpdateIndex());
				return;
			}
			probeEntity.incrementUpdateIndex(1);
		}
		probeEntity.save();
	}
	
	private void processUpdate(IUpdateRecord update) {
		final AbstractEntity localEntity = (AbstractEntity) workspace.findById(update.getEntity().getId());
		
		if(localEntity == null) {
			createEntity(update);
		} else {
			updateEntity(localEntity, update);
		}

	
	}
	
	private void createEntity(IUpdateRecord update) {
		final AbstractEntity remoteEntity = (AbstractEntity) update.getEntity();
		remoteEntity.setWorkspace(workspace);
		if((remoteEntity.getRealmId() & 0xFFFFFFFFL) == 1) { // hack to detect local probe realm
			logger.debug("Setting realm for entity " + remoteEntity + " to " + probe.getEntity().getId());
			remoteEntity.setRealmId(probe.getEntity().getId());
		}
		remoteEntity.save();
		if(update.isAddedToSpace()) {
			remoteEntity.addToSpace(update.getSpaceId());
		}
		
	}
	
	private void updateEntity(AbstractEntity localEntity, IUpdateRecord update) {
		
		localEntity.updateFromEntity(update.getEntity());
		
		if(update.isAddedToSpace()) {
			localEntity.addToSpace(update.getSpaceId());
		}
	}

	
	
	
}
