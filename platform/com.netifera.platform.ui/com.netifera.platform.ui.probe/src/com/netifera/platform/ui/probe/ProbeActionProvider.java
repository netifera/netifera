package com.netifera.platform.ui.probe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.probe.IProbe.ConnectState;
import com.netifera.platform.model.ProbeEntity;
import com.netifera.platform.ui.actions.SpaceAction;
import com.netifera.platform.ui.api.actions.IEntityActionProvider;
import com.netifera.platform.ui.api.actions.ISpaceAction;

public class ProbeActionProvider implements IEntityActionProvider {

	private ILogger logger;
	
	public List<IAction> getActions(IShadowEntity shadow) {
		return Collections.emptyList();
	}

	private void addProbeActions(List<IAction> actions, ProbeEntity probeEntity) {
		final IProbe probe = Activator.getDefault().getProbeManager().getProbeById(probeEntity.getProbeId());
		if(probe == null)
			return;
		if(probe.getConnectState() == ConnectState.CONNECTED) {
			ISpaceAction disconnectAction = new SpaceAction("Disconnect Probe") {
				public void run() {
					probe.disconnect();
				}
			};
			disconnectAction.setImageDescriptor(Activator.getDefault().getImageCache().getDescriptor("icons/disconnect.png"));

			actions.add(disconnectAction);
			
			actions.add(new OpenSpaceAction(probe, logger));
		}
		
		if(probe.getConnectState() == ConnectState.DISCONNECTED) {
			ISpaceAction connectAction = new SpaceAction("Connect Probe") {
				public void run() {
					probe.connect();
				}
			};
			connectAction.setImageDescriptor(Activator.getDefault().getImageCache().getDescriptor("icons/connect.png"));
			actions.add(connectAction);
		}
	}
	
	public List<IAction> getQuickActions(IShadowEntity shadow) {
		List<IAction> actions = new ArrayList<IAction>();
		if(shadow instanceof ProbeEntity) {
			addProbeActions(actions, (ProbeEntity) shadow);
		}
		return actions;
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Probe Action");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}

}
