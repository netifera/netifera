package com.netifera.platform.net.sniffing.stream;

import com.netifera.platform.util.addresses.inet.InternetAddress;

/**
 * An <code>ISessionKey</code> instance uniquely identifies the endpoints of a
 * TCP session being tracked by the framework.
 */
public interface ISessionKey {
	/**
	 * The network address of the host which initiated the connection.
	 * 
	 * @see com.netifera.platform.util.addresses.inet.InternetAddress
	 * 
	 * @return The network address of the host which initiated the connection.
	 */
	InternetAddress getClientAddress();
	
	/**
	 * The network address of the host which accepted the connection.
	 * 
	 * @see com.netifera.platform.util.addresses.inet.InternetAddress
	 * 
	 * @return The network address of the host which accepted the connection.
	 */
	InternetAddress getServerAddress();
	
	/**
	 * The source port of the client side of the connection.
	 * 
	 * @return The source port of the client side of the connection.
	 */
	int getClientPort();
	
	/**
	 * The destination port on the server which accepted the connection.
	 * 
	 * @return The destination port on the server which accepted the connection.
	 */
	int getServerPort();
}
