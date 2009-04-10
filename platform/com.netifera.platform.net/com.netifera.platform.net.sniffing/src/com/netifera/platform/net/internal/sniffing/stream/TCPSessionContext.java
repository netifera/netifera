package com.netifera.platform.net.internal.sniffing.stream;

import com.netifera.platform.net.sniffing.stream.ISessionContext;
import com.netifera.platform.net.sniffing.stream.ISessionKey;

public class TCPSessionContext implements ISessionContext {

	private final ISessionKey key;
	private final Object tag;
	TCPSessionContext(ISessionKey key, Object sessionTag) {
		this.key = key;
		this.tag = sessionTag;
	}
	public ISessionKey getKey() {
		return key;
	}
	public Object getSessionTag() {
		return tag;
	}

}
