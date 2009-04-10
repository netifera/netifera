package com.netifera.platform.internal.dispatcher.channels;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.netifera.platform.api.channels.IChannelMessageSerializer;
import com.netifera.platform.api.dispatcher.IClientDispatcher;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.internal.dispatcher.Messenger;
import com.netifera.platform.internal.dispatcher.ServerDispatcher;

public class LocalChannel {
	private final static boolean DEBUG = false;
	private static final int MESSAGE_QUEUE_SIZE = 16;
	
	private final BlockingQueue<IProbeMessage> q1 = new ArrayBlockingQueue<IProbeMessage>(MESSAGE_QUEUE_SIZE);
	private final BlockingQueue<IProbeMessage> q2 = new ArrayBlockingQueue<IProbeMessage>(MESSAGE_QUEUE_SIZE);
	private final IChannelMessageSerializer serverSerializer;
	private final IChannelMessageSerializer clientSerializer;
	private Messenger clientMessenger;
	
	
	public static LocalChannel create(IClientDispatcher clientDispatcher, ServerDispatcher serverDispatcher, ILogger logger) {
		logger.debug("Activating Local Channel");
		final LocalChannel localChannel = new LocalChannel(logger);
		serverDispatcher.registerLocalConnection(localChannel.serverSerializer);
		localChannel.clientMessenger = new Messenger(localChannel.clientSerializer, clientDispatcher, logger);
		localChannel.clientMessenger.start();
		return localChannel;
	}
	
	
	private LocalChannel(ILogger logger) {
		final ILogger serverLogger = logger.getManager().getLogger("Local Channel [server]");
		final ILogger clientLogger = logger.getManager().getLogger("Local Channel [client]");
		if(DEBUG) {
			serverLogger.enableDebug();
			clientLogger.enableDebug();
		}
		serverSerializer = new LocalChannelSerializer(q1, q2, serverLogger);
		clientSerializer = new LocalChannelSerializer(q2, q1, clientLogger);
	}

	
	public IMessenger getClientMessenger() {
		return clientMessenger;
	}
	

	
	
}
