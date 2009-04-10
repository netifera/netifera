package com.netifera.platform.internal.model.sync;

import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.internal.model.Workspace;
import com.netifera.platform.model.IUpdateRecord;
import com.netifera.platform.model.ModelUpdate;
/*
 * Handles synchronization for a single connection to the probe.
 */
public class ChannelSynchronizer extends Thread {
	private final static int MAX_UPDATES_PER_MESSAGE = 25;
	final private ModelSynchronizer synchronizer;
	final private Workspace workspace;
	final private IMessenger messenger;
	final private ILogger logger;
	private long currentUpdateIndex;
	private long nextSendIndex;
	private volatile boolean done;
	
	ChannelSynchronizer(ModelSynchronizer synchronizer, IMessenger messenger, ILogger logger, long startIndex) {
		this.synchronizer = synchronizer;
		this.messenger = messenger;
		this.logger = logger;
		this.done = false;
		this.nextSendIndex = startIndex;
		this.workspace = synchronizer.getWorkspace();
		workspace.addEntityUpdateListener(new IEventHandler() {
			public void handleEvent(IEvent event) {
				event();	
			}
		});
	}

	private synchronized void event() {
		logger.debug("Event! update index = " + synchronizer.getWorkspace().getCurrentUpdateIndex());
		currentUpdateIndex = synchronizer.getWorkspace().getCurrentUpdateIndex();
		notifyAll();
	}
	
	public void kill() {
		done = true;
		notifyAll();
	}
	
	public void run() {
		currentUpdateIndex = synchronizer.getWorkspace().getCurrentUpdateIndex();

		logger.debug("Starting Channel Synchronizer");
		while(!done) {
			try {
				sendUpdates();
			} catch (InterruptedException e) {
				interrupt();
				done = true;
			}
		}
		synchronizer.removeChannelSynchronizer(messenger);
	}
	
	private void sendUpdates() throws InterruptedException {
		synchronized(this) {
			logger.debug("CurrentUpdateIndex: "  + currentUpdateIndex + " nextSendIndex: " + nextSendIndex);
			while(!done && currentUpdateIndex <= nextSendIndex) {
				wait();
			}
		}
		sendUpdateRecords(currentUpdateIndex);
	}
	
	private void sendUpdateRecords(long current) {
		final List<IUpdateRecord> updates = new ArrayList<IUpdateRecord>(MAX_UPDATES_PER_MESSAGE);
		while(!done && nextSendIndex < current) {	
			updates.clear();
			for(int i = 0; i < MAX_UPDATES_PER_MESSAGE && nextSendIndex < current; i++) {
				IUpdateRecord record = workspace.getEntityByUpdateIndex(nextSendIndex);
				if(record == null)
					break;
				updates.add(record);
				nextSendIndex++;
			}
			sendUpdateMessage(updates);	
		}
	}
	
	private void sendUpdateMessage(List<IUpdateRecord> updates) {
		if(updates.isEmpty())
			return;
		final ModelUpdate updateMessage = new ModelUpdate(updates);
		try {
			messenger.emitMessage(updateMessage);
		} catch (MessengerException e) {
			interrupt();
			done = true;
		}
	}
}
