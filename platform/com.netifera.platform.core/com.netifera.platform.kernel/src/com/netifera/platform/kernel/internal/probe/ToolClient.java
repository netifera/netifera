package com.netifera.platform.kernel.internal.probe;

import com.netifera.platform.api.dispatcher.MessageErrorException;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.dispatcher.UnhandledMessageException;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tasks.ITaskClient;
import com.netifera.platform.api.tools.IToolConfiguration;
import com.netifera.platform.tools.ToolLaunchMessage;

public class ToolClient {

	private final IProbe probe;
	private final ILogger logger;
	private final ITaskClient taskClient;
	
	ToolClient(IProbe probe, ILogger logger) {
		this.probe = probe;
		this.taskClient = probe.getTaskClient();
		this.logger = logger.getManager().getLogger("Tool Client");
	}
	
	void launchTool(final String toolClassName, final IToolConfiguration configuration, final ISpace space) {
		final Thread launchThread = new Thread(new Runnable() {
			public void run() {
				doLaunch(toolClassName, configuration, space);
			}
			
		});
		launchThread.start();
	}
	
	private void doLaunch(String toolClassName, IToolConfiguration configuration, ISpace space) {
		if(!probe.isConnected() || probe.getMessenger() == null) {
			logger.warning("Failed to launch tool because probe is not connected");
			return;
		}
		
		logger.debug("Sending message to probe id = " + probe.getProbeId());
		try {
			exchangeMessages(toolClassName, configuration, space);
		} catch(UnhandledMessageException e) {
			logger.warning("Tool launch failed because tool launcher not found on probe");
		}catch(MessageErrorException e) {
			logger.warning("Tool launch failed: " + e.getMessage(), e);
		} catch (MessengerException e) {
			logger.warning("Failed to send tool launch message: " + e.getMessage(), e);
		}
	}
	
	private void exchangeMessages(String toolClassName, IToolConfiguration configuration, ISpace space) throws MessengerException {
		logger.debug("Sending tool launch message to launch: " + toolClassName);
		ToolLaunchMessage response = (ToolLaunchMessage) probe.getMessenger().exchangeMessage(new ToolLaunchMessage(toolClassName, configuration, space.getId()));
		logger.debug("Launch completed, task id = " + response.getTaskId());
		taskClient.createTask(toolClassName, response.getTaskId(), space);
		taskClient.startTask(response.getTaskId());
	}
	
	
}
