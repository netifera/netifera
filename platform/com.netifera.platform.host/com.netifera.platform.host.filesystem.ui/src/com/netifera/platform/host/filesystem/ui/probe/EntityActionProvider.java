package com.netifera.platform.host.filesystem.ui.probe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.host.filesystem.IFileSystemFactory;
import com.netifera.platform.model.ProbeEntity;
import com.netifera.platform.ui.api.actions.IEntityActionProvider;

public class EntityActionProvider implements IEntityActionProvider {

	private ILogger logger;
	private IProbeManagerService probeManager;
	private IFileSystemFactory fileSystemFactory;
	
	public List<IAction> getActions(IShadowEntity shadow) {
		List<IAction> actions = new ArrayList<IAction>();
		if(shadow instanceof ProbeEntity) {
			ProbeEntity probeEntity = (ProbeEntity) shadow;
			IProbe probe = probeManager.getProbeById(probeEntity.getProbeId());
			if(probe != null && probe.isConnected())
				actions.add(new OpenProbeFileSystemViewAction(logger, probe, fileSystemFactory));
		}
		return actions;
	}

	public List<IAction> getQuickActions(IShadowEntity shadow) {
		return Collections.emptyList();
	}

	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Probe Actions");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}
	protected void setProbeManager(IProbeManagerService probeManager) {
		this.probeManager = probeManager;
	}
	
	protected void unsetProbeManager(IProbeManagerService probeManager) {
		
	}
	
	protected void setFileSystemFactory(IFileSystemFactory factory) {
		fileSystemFactory = factory;
	}
	
	protected void unsetFileSystemFactory(IFileSystemFactory factory) {
		
	}
}
