package com.netifera.platform.net.internal.daemon.sniffing;

import java.nio.ByteBuffer;

import com.netifera.platform.net.daemon.sniffing.IStreamSnifferConfig;
import com.netifera.platform.net.daemon.sniffing.ITCPStreamSniffer;
import com.netifera.platform.net.sniffing.stream.ISessionContext;
import com.netifera.platform.net.sniffing.stream.IStreamSniffer;

public class StreamHandler implements IStreamSniffer {

	final ITCPStreamSniffer sniffer;

	static class StreamConfig implements IStreamSnifferConfig {

		private boolean clientRequired = false;
		private boolean serverRequired = false;
		public void setClientRequired() {
			clientRequired = true;
		}

		public void setServerRequired() {
			serverRequired = true;
		}
		
		public boolean getServerRequired() {
			return serverRequired;
		}
		
		public boolean getClientRequired() {
			return clientRequired;
		}
	}
	
	private final StreamConfig config;
	private final ISniffingModuleOutput output;
	private final long spaceId;
	StreamHandler(long spaceId, ITCPStreamSniffer sniffer, ISniffingModuleOutput output) {
		this.sniffer = sniffer;
		this.spaceId = spaceId;
		this.output = output;
		config = new StreamConfig();
		sniffer.initialize(config);
	}
	
	public boolean handleNewSession(ISessionContext ctx, SessionType type) {		
		switch(type) {
		case CLIENT_ONLY:
			if(config.getServerRequired()) {
				return false;
			}
			break;
			
		case SERVER_ONLY:
			if(config.getClientRequired()) {
				return false;
			}
			break;
			
		case FULL_SESSION:
				break;
		}
	
		return sniffer.handleNewSession(new StreamModuleContext(ctx, spaceId, output));
	
	}
	
	public boolean handleClientData(ISessionContext ctx, ByteBuffer data) {
		return sniffer.handleClientData(new StreamModuleContext(ctx, spaceId, output), data);
	}

	public boolean handleServerData(ISessionContext ctx, ByteBuffer data) {
		return sniffer.handleServerData(new StreamModuleContext(ctx, spaceId, output), data);
	}

	public void handleSessionClose(ISessionContext ctx) {
		sniffer.handleSessionClose(new StreamModuleContext(ctx, spaceId, output));
	}
	
}
