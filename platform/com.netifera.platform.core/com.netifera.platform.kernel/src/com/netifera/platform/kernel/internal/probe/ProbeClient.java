package com.netifera.platform.kernel.internal.probe;

import com.netifera.platform.api.channels.IChannelConnectProgress;
import com.netifera.platform.api.channels.IChannelConnecter;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tasks.ITaskClient;
import com.netifera.platform.api.tools.IToolConfiguration;
import com.netifera.platform.model.ModelUpdate;
import com.netifera.platform.model.ProbeEntity;
import com.netifera.platform.model.RequestModelUpdate;
import com.netifera.platform.model.SetModelIdPrefix;

public class ProbeClient implements IProbe {
	private final ProbeManagerService probeManager;
	private final ProbeEntity probeEntity;
	private final ILogger logger;
	private final ToolClient toolClient;
	private final TaskClient taskClient;
	private final ModelSyncClient modelSyncClient;
	private IMessenger messenger;
	private final IChannelConnectProgress connectProgress;
	private IChannelConnecter channelConnecter;
	private ConnectState connectState = ConnectState.DISCONNECTED;
	private String connectError = "";
	
	
	ProbeClient(ProbeManagerService probeManager, ProbeEntity entity, ILogger logger) {
		this.probeManager = probeManager;
		this.probeEntity = entity;
		this.logger = logger.getManager().getLogger("Probe Client");
		this.logger.enableDebug();
		this.connectProgress = createConnectProgress();
		
		taskClient = new TaskClient(this, entity.getWorkspace(), logger);
		toolClient = new ToolClient(this, logger);
		modelSyncClient = new ModelSyncClient(this, entity.getWorkspace(), logger);

	}
	
	private IChannelConnectProgress createConnectProgress() {
		return new IChannelConnectProgress() {
			public void connectCompleted(IMessenger channelMessenger) {
				setConnected(channelMessenger);
			}
			public void connectFailed(String reason, Throwable exception) {
				setConnectFailed(reason, exception);
				
			}
			public void connectUpdate(String update) {
				processConnectUpdate(update);				
			}	
		};
	}
	
	public void setConnected(IMessenger channelMessenger) {
			connectState = ConnectState.CONNECTED;
			this.messenger = channelMessenger;
			messenger.setProbe(this);
			if(!isLocalProbe()) {
				setModelPrefixId(messenger);
				startModelUpdates(messenger);
			}
			notifyChange();
	}
	
	private void setModelPrefixId(IMessenger messenger) {
		try {
			messenger.sendMessage(new SetModelIdPrefix(getProbeId()));
			logger.debug("Send prefix id to probe");
		} catch (MessengerException e) {
			logger.warning("Failed to set model id prefix: " + e.getMessage());
		}
		
	}
	
	private void startModelUpdates(IMessenger messenger) {
		try {
			messenger.sendMessage(new RequestModelUpdate(probeEntity.getUpdateIndex()));
		} catch(MessengerException e) {
			logger.warning("Failed to send request model update message " + e.getMessage());
		}
	}
	
	

	private void setConnectFailed(String reason, Throwable exception) {
		connectState = ConnectState.CONNECT_FAILED;
		connectError = reason;
		notifyChange();		
	}
	
	private void processConnectUpdate(String update) {
		// XXX implement me
	}
	
	public void notifyChange() {
		probeManager.fireChangeEvent();
		probeEntity.update();
	}
	
	public IMessenger getMessenger() {
		return messenger;
	}

	public ITaskClient getTaskClient() {
		return taskClient;
	}
	
	
	public IEntity getEntity() {
		return probeEntity;
	}

	public long getProbeId() {
		return probeEntity.getProbeId();
	}

	public ConnectState getConnectState() {
		return connectState;
	}
	public String getConnectError() {
		return connectError;
	}
	void setConnectState(ConnectState newState) {
		this.connectState = newState;
	}
	
	public void connect() {
		if(!isDisconnected()) {
			logger.warning("Connect called on probe which is not disconnected");
			return;
		}
		channelConnecter = probeManager.createChannelConnecter(probeEntity.getChannelConfig());
		if(channelConnecter == null) {
			logger.warning("Failed to convert entity channel configuration into a channel connecter.  Config = " 
					+ probeEntity.getChannelConfig());
			return;
		}
		this.connectState = ConnectState.CONNECTING;
		channelConnecter.connect(connectProgress);
		notifyChange();
	}
	
	public void disconnect() {
		messenger.close();
	}
	
	public boolean isLocalProbe() {
		return probeEntity.isLocal();
	}
	public String getName() {
		return probeEntity.getName();
	}
	public boolean isConnected() {
		return connectState == ConnectState.CONNECTED;
	}
	
	public boolean isDisconnected() {
		return connectState == ConnectState.DISCONNECTED || connectState == ConnectState.CONNECT_FAILED;
	}
	

	public void launchTool(final String toolClassName, String toolBundle,
			final IToolConfiguration configuration, ISpace space) {
		toolClient.launchTool(toolClassName, configuration, space);
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof ProbeClient)) {
			return false;
		}
		return ((ProbeClient)o).getProbeId() == getProbeId();
		
	}
	public int hashCode() {
		return (int) getProbeId();
	}
	
	public String toString() {
		return "Probe [" + probeEntity.getName() + "]";
	}
	
	
	public void setDisconnected() {
		connectState = ConnectState.DISCONNECTED;
		this.messenger = null;
		notifyChange();
	}

	void processModelUpdate(ModelUpdate update) {
		modelSyncClient.processModelUpdate(update);
	}
}
