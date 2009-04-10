package com.netifera.platform.net.internal.daemon.sniffing;

import com.netifera.platform.net.daemon.sniffing.IStreamModuleContext;
import com.netifera.platform.net.sniffing.stream.ISessionContext;
import com.netifera.platform.net.sniffing.stream.ISessionKey;

public class StreamModuleContext implements IStreamModuleContext {

	private final ISessionContext sessionContext;
	private final ISniffingModuleOutput output;
	private final long spaceId;
	
	StreamModuleContext(ISessionContext ctx, long spaceId, ISniffingModuleOutput output) {
		this.sessionContext = ctx;
		this.spaceId = spaceId;
		this.output = output;
	}
	
	public ISessionKey getKey() {
		return sessionContext.getKey();
	}

	public void printOutput(String message) {
		output.printOutput(message);
	}
	
	public long getRealm() {
		final Object o = sessionContext.getSessionTag();
		if(o == null || !(o instanceof Long))
			return -1;
		return ((Long)o).longValue();
	}
	public long getSpaceId() {
		return spaceId;
	}

}
