package com.netifera.platform.net.daemon.sniffing;

import java.nio.ByteBuffer;

/**
 * This interface should be implemented by sniffing modules which wish to
 * process assembled TCP stream data.  Callback methods are provided for
 * notification when a session is established or closed, as well as when data is
 * received in either direction.
 * 
 * <p>If your module only needs to examine the initial traffic exchanged between
 * the client and server, consider implementing the simpler {@link
 * ITCPBlockSniffer} instead.</p>
 * 
 * @see ITCPBlockSniffer
 */
public interface ITCPStreamSniffer extends ISniffingModule {
	
	/**
	 * Initialize this module and allow it to configure stream sniffing
	 * parameters through the <code>IStreamSnifferConfig</code> interface.
	 * 
	 * @see IStreamSnifferConfig
	 * @param config An instance of the configuration object for configuring a
	 * stream sniffer.
	 */
	void initialize(IStreamSnifferConfig config);
	
	/**
	 * This callback is called for each new established session.
	 * 
	 * <p>By returning <code>true</code> from this method, a module indicates
	 * that it wishes to continue receiving callbacks for this session. A return
	 * value of <code>false</code> indicates that the module is not interested
	 * in receiving any further callbacks for this session.</p>
	 *   
	 * @param ctx Context for processing this information.
	 * @return Return false to stop tracking this session, true otherwise.
	 */
	boolean handleNewSession(IStreamModuleContext ctx);
	
	/**
	 * This callback delivers session data transmitted from the client
	 * to the server.
	 * 
	 * @param ctx Context for processing this information.
	 * @param data Data bytes captured from stream. 
	 * @return Return false to stop tracking this session, true otherwise.
	 */
	boolean handleClientData(IStreamModuleContext ctx, ByteBuffer data);
	
	
	/**
	 * This callback delivers session data transmitted from the server to the
	 * client.
	 * 
	 * @param ctx Context for processing this information.
	 * @param data Data bytes captured from stream. 
	 * @return Return false to stop tracking this session, true otherwise.
	 */
	boolean handleServerData(IStreamModuleContext ctx, ByteBuffer data);
	
	/**
	 * This callback is called when a session is closed.
	 * 
	 * @param ctx Context for the closed session.
	 */
	void handleSessionClose(IStreamModuleContext ctx);
}
