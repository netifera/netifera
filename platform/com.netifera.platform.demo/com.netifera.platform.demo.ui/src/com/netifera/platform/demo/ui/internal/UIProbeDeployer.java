package com.netifera.platform.demo.ui.internal;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.SocketChannel;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.netifera.platform.api.channels.IChannelRegistry;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.demo.IProbeDeployer;
import com.netifera.platform.net.model.INetworkEntityFactory;

public class UIProbeDeployer implements IProbeDeployer {

	private final int DEFAULT_CONNECTBACK_PORT = 54321;
	
	private static final String DEMO_PLUGIN = "com.netifera.platform.demo.ui";
	private static final String PROBE_FILENAME = "probe_linux_i386.plb";
	private ILogger logger;
	private IChannelRegistry channelRegistry;
	private IProbeManagerService probeManager;
	private INetworkEntityFactory networkEntityFactory;
	private IModelService model;
	
	public void deployProbe(final SocketChannel socket, final long realmId, final long spaceId) {
		
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			public void run() {
				runDeployProbe(socket, realmId, spaceId);
				
			}
			
		});
	}
	
	IModelService getModel() {
		return model;
	}
	
	IProbeManagerService getProbeManager() {
		return probeManager;
	}
	
	ILogger getLogger() {
		return logger;
	}
	
	INetworkEntityFactory getNetworkEntityFactory() {
		return networkEntityFactory;
	}
	
	public void runDeployProbe(SocketChannel socket, long realmId, long spaceId) {
	
		String path = getProbePath();
		if(!verifyProbePath(path)) {
			
		}
		ProbeDeployRunnable runnable = new ProbeDeployRunnable(socket, new File(path),
			channelRegistry.createConnecter("connectback:" + DEFAULT_CONNECTBACK_PORT), this, realmId, spaceId);
		
		final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		final ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(true, true, runnable);
		} catch (InvocationTargetException e) {
			logger.error("Error transferring probe", e);
		} catch (InterruptedException e) {
		}
	}
	
	private String getProbePath() {
		if(isRunningInEclipse()) 
			return getEclipseProbePath();
		 else
			return getBuildProbePath();
	}
	
	private String getEclipseProbePath() {
		final String basePath = getBasePathForEclipse();
		if(basePath == null) {
			logger.error("Could not locate base path for probe binary");
			return null;
		}
		final String separator = System.getProperty("file.separator");
		return basePath + DEMO_PLUGIN + separator + PROBE_FILENAME;
		
	}
	
	private String getBuildProbePath() {
		final String basePath = getBasePathForBuild();
		if(basePath == null) {
			logger.error("Could not locate base path for probe binary");
			return null;
		}
		return basePath + PROBE_FILENAME;
	}
	
	private String getBasePathForBuild() {
		final String installArea =  System.getProperty("osgi.install.area");
		if(installArea == null || !installArea.startsWith("file:")) {
			return null;
		}
		return installArea.substring(5);
	}
	
	private String getBasePathForEclipse() {
		final String configArea = System.getProperty("osgi.configuration.area");
		if(configArea == null || !configArea.startsWith("file:")) {
			return null;
		}
		final String trimmedPath = configArea.substring(5);
		int metadataIndex = trimmedPath.indexOf(".metadata");
		if(metadataIndex == -1)
			return null;
		return trimmedPath.substring(0, metadataIndex);
	}
	private boolean isRunningInEclipse() {
		return System.getProperty("osgi.dev") != null;
	}
	
	private boolean verifyProbePath(String path) {
		if(path == null) { 
			return false;
		}
		File file = new File(path);
		if (!file.exists()) {
			return false;
		} 
		logger.info("Probe  located at " + path);
		return true;
	}
	
	
	protected void setLogManager(ILogManager logManager) {
		this.logger = logManager.getLogger("Probe Deployer");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}
	
	protected void setChannelRegistry(IChannelRegistry registry) {
		channelRegistry = registry;
	}
	
	protected void unsetChannelRegistry(IChannelRegistry registry) {
		
	}
	
	protected void setProbeManager(IProbeManagerService probeManager) {
		this.probeManager = probeManager;
	}
	
	protected void unsetProbeManager(IProbeManagerService probeManager) {
		
	}
	
	protected void setModel(IModelService model) {
		this.model = model;
		
	}
	
	protected void unsetModel(IModelService model) {
		
	}
	
	protected void setNetworkEntityFactory(INetworkEntityFactory factory) {
		networkEntityFactory = factory;
	}

	protected void unsetNetworkEntityFactory(INetworkEntityFactory factory) {
		
	}
	

}
