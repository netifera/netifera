package com.netifera.platform.host.internal.terminal.ui.pty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.model.ProbeEntity;
import com.netifera.platform.ui.api.actions.IEntityActionProvider;

public class EntityActionProvider implements IEntityActionProvider {

	private IProbeManagerService probeManager;
	private ILogger logger;
	
	public List<IAction> getActions(IShadowEntity shadow) {
		List<IAction> actions = new ArrayList<IAction>();
		if(shadow instanceof ProbeEntity) {
			ProbeEntity probeEntity = (ProbeEntity) shadow;
			IProbe probe = probeManager.getProbeById(probeEntity.getProbeId());
			if(probe.isConnected()) {
				actions.add(new OpenProbePtyTerminalAction(probe, logger));
				
			}
		}
		return actions;
	}

	public List<IAction> getQuickActions(IShadowEntity shadow) {
		return Collections.emptyList();
	}

	protected void setProbeManager(IProbeManagerService probeManager) {
		this.probeManager = probeManager;
	}
	
	protected void unsetProbeManager(IProbeManagerService probeManager) {
		
	}
}
