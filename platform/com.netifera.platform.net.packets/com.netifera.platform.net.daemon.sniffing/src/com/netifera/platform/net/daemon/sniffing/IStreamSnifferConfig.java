package com.netifera.platform.net.daemon.sniffing;

/**
 * This interface provides access to some configurable parameters for an
 * instance of {@link ITCPStreamSniffer}.
 * 
 * <p>In some environments only one side of a TCP session may be visible to
 * netifera.</p>
 * 
 * <p>As an example, while sniffing wireless traffic, it is more common to see
 * traffic from a server to a client since the client side of a session is often
 * a device which transmits with much less power than the access point which
 * transmits the data received from the server.</p>
 * 
 * <p>If you are writing a module which requires traffic in one particular
 * direction (client-to-server or server-to-client) or if it requires seeing
 * traffic in both directions you can declare that requirement by calling the
 * methods in this configuration interface.</p>
 */
public interface IStreamSnifferConfig {
	/**
	 * Call this method to indicate that visibility of traffic from the server
	 * to the client is required for this module to function correctly.
	 * 
	 * <p>This module will only be notified that a session exists if the server
	 * to client traffic is available for that session.</p>
	 */
	void setServerRequired();
	
	/**
	 * Call this method to indicate that visibility of traffic from the client
	 * to the server is required for this module to function correctly.
	 * 
	 * <p>This module will only be notified that a session exists if the client
	 * to server traffic is available for that session.</p>
	 */
	void setClientRequired();
}
