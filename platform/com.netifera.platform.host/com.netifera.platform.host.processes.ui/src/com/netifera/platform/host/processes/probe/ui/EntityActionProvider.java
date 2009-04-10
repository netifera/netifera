package com.netifera.platform.host.processes.probe.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.host.internal.processes.ui.Activator;
import com.netifera.platform.host.processes.IProcessManagerFactory;
import com.netifera.platform.model.ProbeEntity;
import com.netifera.platform.ui.api.actions.IEntityActionProvider;

public class EntityActionProvider implements IEntityActionProvider {

	private ILogger logger;
	private IProcessManagerFactory processManagerFactory;
	
	public List<IAction> getActions(IShadowEntity shadow) {
		List<IAction> actions = new ArrayList<IAction>();
		if(shadow instanceof ProbeEntity) {
			ProbeEntity probeEntity = (ProbeEntity) shadow;
			IProbe probe = Activator.getInstance().getProbeManager().getProbeById(probeEntity.getProbeId());
			if(probe != null && probe.isConnected())
				actions.add(new OpenProbeProcessViewAction(logger, probe, processManagerFactory));
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
	
	protected void setProcessManagerFactory(IProcessManagerFactory factory) {
		this.processManagerFactory = factory;
	}
	
	protected void unsetProcessManagerFactory(IProcessManagerFactory factory) {
		
	}
}
