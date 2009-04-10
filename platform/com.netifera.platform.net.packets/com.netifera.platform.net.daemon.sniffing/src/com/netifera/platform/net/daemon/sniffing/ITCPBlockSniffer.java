package com.netifera.platform.net.daemon.sniffing;

import java.nio.ByteBuffer;

import com.netifera.platform.net.sniffing.stream.IBlockSnifferConfig;

/**
 * This interface provides a convenient way to process only the initially
 * exchanged traffic from each TCP session. The first bytes in both directions
 * are collected and once a limit has been exceeded, the {@link #handleBlock()}
 * method is called with all of the received data in both directions.  
 * 
 * <p>The limit can be defined as a maximum number of bytes for one of the
 * following conditions:
 * <ul>
 *  <li>Client to server traffic</li>
 *  <li>Server to client traffic</li>
 *  <li>Total volume of all traffic exchanged between client and server.</li>
 * </ul></p>
 *  
 * <p>This limit is configured in the {@link #initialize(IBlockSnifferConfig)}
 * method through the {@link ITCPBlockSniffer} interface.</p>
 * 
 * <p>Modules that need to capture or examine all session traffic, or that need
 * to process traffic immediately as it arrives should implement the {@link
 * ITCPStreamSniffer} interface instead of this one.</p>
 * 
 * @see com.netifera.platform.net.sniffing.stream.IBlockSnifferConfig
 * @see ITCPStreamSniffer
 */
public interface ITCPBlockSniffer extends ISniffingModule {
	/**
	 * Initialize this module by allowing it to configure the block sniffing
	 * parameters through the <code>IBlockSnifferConfig</code> interface.
	 * 
	 * @see com.netifera.platform.net.sniffing.stream.IBlockSnifferConfig
	 * 
	 * @param config An instance of the configuration object for configuring the
	 * block sniffing parameters. 
	 */
	void initialize(IBlockSnifferConfig config);
	
	
	/**
	 * This callback is called once for each tracked TCP session once the
	 * traffic exchanged has exceeded the limit declared in the {@link
	 * #initialize(IBlockSnifferConfig)} method.
	 * 
	 * <p>All of the network traffic in both directions seen since the beginning
	 * of the session is passed in the <code>clientData</code> and <code>
	 * serverData</code> parameters.</p>
	 * 
	 * @param ctx Context for processing this information.
	 * @param clientData Bytes sent from the client to the server.
	 * @param serverData Bytes sent from the server to the client.
	 */
	void handleBlock(IStreamModuleContext ctx, ByteBuffer clientData,
			ByteBuffer serverData);
}
