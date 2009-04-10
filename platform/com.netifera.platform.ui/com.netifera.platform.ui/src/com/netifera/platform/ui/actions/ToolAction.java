package com.netifera.platform.ui.actions;

import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.ui.UIPlugin;
import com.netifera.platform.ui.api.actions.IToolAction;

public class ToolAction extends SpaceAction implements IToolAction {
	private final String className;
//	private final String bundleName;
	private List<String> dependencies = new ArrayList<String>();
	
	public ToolAction(String name, String className) {
		super(name);
		this.className = className;
//		this.bundleName = bundleName;
	}

	public List<String> getBundleDependencies() {
		return dependencies;
	}
	
/*	public String getBundleName() {
		return bundleName;
	}
*/
	public String getClassName() {
		return className;
	}

	@Override
	public void run() {
		final IProbeManagerService probeManager = UIPlugin.getPlugin().getProbeManager();
		final IProbe probe = probeManager.getProbeById(getSpace().getProbeId());
		if(!probe.isConnected()) {
			throw new IllegalArgumentException("Cannot launch tool because probe is not connected");
		}
		if(getSpace() == null) {
			throw new IllegalArgumentException("Cannot launch tool because no space has been set for action " + this);
		}
		
		probe.launchTool(getClassName(), "", getConfiguration(), getSpace());
	}
}
