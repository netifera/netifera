package com.netifera.platform.api.probe;


import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.tasks.ITaskClient;
import com.netifera.platform.api.tools.IToolConfiguration;
/**
 * This is the client interface to probe services.
 *
 */
public interface IProbe {
	enum ConnectState { DISCONNECTED, CONNECTING, CONNECTED, CONNECT_FAILED };
	
	IEntity getEntity();
	
	IMessenger getMessenger();
	String getName();
	void connect();
	void disconnect();
	boolean isLocalProbe();
	boolean isConnected();
	boolean isDisconnected();
	ConnectState getConnectState();
	String getConnectError();
	void setConnected(IMessenger messenger);
	void setDisconnected();
	
	long getProbeId();
	
	ITaskClient getTaskClient();
		
	/*
	 * Actions may call this directly.
	 */
	void launchTool(String toolClass, String toolBundle, IToolConfiguration configuration, ISpace space);

}
