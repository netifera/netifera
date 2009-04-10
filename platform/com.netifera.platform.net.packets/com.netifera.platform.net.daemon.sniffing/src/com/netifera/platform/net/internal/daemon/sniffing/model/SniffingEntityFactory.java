package com.netifera.platform.net.internal.daemon.sniffing.model;

import java.util.List;

import com.netifera.platform.api.model.IModelPredicate;
import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.net.daemon.sniffing.model.CaptureFileEntity;
import com.netifera.platform.net.daemon.sniffing.model.ISniffingEntityFactory;
import com.netifera.platform.net.daemon.sniffing.model.NetworkInterfaceEntity;
import com.netifera.platform.net.daemon.sniffing.model.SniffingSessionEntity;

public class SniffingEntityFactory implements ISniffingEntityFactory {

	private IModelService model;
	
	protected void setModelService(IModelService model) {
		this.model = model;
	}
	
	protected void unsetModelService(IModelService model) {
		this.model = null;
	}
	
	
	public SniffingSessionEntity createSniffingSession(long realm, long spaceId) {
		return SniffingSessionEntity.create(getWorkspace(), realm, spaceId);
	}
	
	public CaptureFileEntity createCaptureFile(long realm, long view, String path) {
		return CaptureFileEntity.create(getWorkspace(), realm, view, path);
	}

	public NetworkInterfaceEntity createNetworkInterface(final long realm, long spaceId, final String name) {
		List<NetworkInterfaceEntity> results = getWorkspace().findByPredicate(NetworkInterfaceEntity.class,
				new IModelPredicate<NetworkInterfaceEntity>() {

					public boolean match(NetworkInterfaceEntity candidate) {
						return candidate.getRealmId() == realm &&
							candidate.getName().equals(name);
					}
						
					
				});
		if(results.size() > 0) {
			return results.get(0);
		}
		
		return NetworkInterfaceEntity.create(getWorkspace(), realm, spaceId, name);
	
	}

	private IWorkspace getWorkspace() {
		if(model.getCurrentWorkspace() == null) {
			throw new IllegalStateException("Cannot create sniffing entities because no workspace is currently open");
		}
		return model.getCurrentWorkspace();
	}


}
