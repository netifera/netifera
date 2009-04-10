package com.netifera.platform.net.sniffing.stream;

import java.nio.ByteBuffer;


public interface IStreamSniffer {
	enum SessionType { FULL_SESSION, CLIENT_ONLY, SERVER_ONLY };
	
	/**
	 * 
	 * @param ctx
	 * @param type
	 * @return False to indicate that this session not be tracked
	 */
	boolean handleNewSession(ISessionContext ctx, SessionType type);
	
	/**
	 * 
	 * @param ctx
	 * @param data
	 * @return False to stop tracking this session.
	 */
	boolean handleClientData(ISessionContext ctx, ByteBuffer data);
	
	/**
	 * 
	 * @param ctx
	 * @param data
	 * @return False to stop tracking this session.
	 */
	boolean handleServerData(ISessionContext ctx, ByteBuffer data);
	void handleSessionClose(ISessionContext ctx);
}
