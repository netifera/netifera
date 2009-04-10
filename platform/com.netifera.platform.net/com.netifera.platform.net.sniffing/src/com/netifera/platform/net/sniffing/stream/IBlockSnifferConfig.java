package com.netifera.platform.net.sniffing.stream;

/**
 * When a sniffing module which implements <tt>ITCPBlockSniffer</tt> is
 * initialized, the method {@link ITCPBlockSniffer#initialize
 * (IBlockSnifferConfig)} is called and passed an instance of this interface.
 * 
 * <p>The module may call the methods provided by this interface to configure
 * the behavior of the TCP block assembly component of the sniffing engine and
 * specify limits on the quantity of traffic which will cause delivery of data
 * to the module.</p>
 */
public interface IBlockSnifferConfig {
	
	/**
	 * Request that session data be delivered when the number of bytes
	 * transferred from from the client to the server has exceeded the specified
	 * limit.
	 * 
	 * @param limit The number of bytes which will trigger delivery of data to
	 * the module.
	 */
	void setClientLimit(int limit);
	
	/**
	 * Request that session data be delivered when the number of bytes
	 * transferred from the server to the client has exceeded the specified
	 * limit.
	 * 
	 * @param limit The number of bytes which will trigger delivery of data to
	 * the module.
	 */
	void setServerLimit(int limit);
	
	/**
	 * Request that session data be delivered when the sum of bytes transferred
	 * in both directions exceeds the specified limit.
	 * 
	 * @param limit The total number of bytes (in both directions) which will
	 * trigger delivery of data to the module.
	 */
	void setTotalLimit(int limit);
}
