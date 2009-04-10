package com.netifera.platform.net.sniffing.stream;


public interface ISessionContext {
	/**
	 * The session key which identifies this session.
	 * @return
	 */
	ISessionKey getKey();
	
	Object getSessionTag();
	
}
