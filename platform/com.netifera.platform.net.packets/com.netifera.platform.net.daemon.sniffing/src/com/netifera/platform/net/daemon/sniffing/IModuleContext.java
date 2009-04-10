package com.netifera.platform.net.daemon.sniffing;

public interface IModuleContext {
	/**
	 * Print a string to the UI console.
	 * 
	 * <p>This functionality exists to assist in developing and debugging
	 * sniffing daemon modules.</p>
	 *   
	 * <p><i>Production modules should avoid writing output to the console.</i>
	 * </p>
	 * 
	 * @param message The message to display in the console.
	 */
	void printOutput(String message);
	
	/**
	 * Returns the id value of the realm this packet was captured in.  Realms 
	 * are subsets of the entire data model where each entity is required to
	 * be uniquely identified.  As an example, two identical IP addresses can
	 * exist in the model but they must be in separate realms.  This allows 
	 * for representation of different private internal networks that have
	 * the same allocation of network ranges (ie: 192.168.x.x).
	 * 
	 * @see com.netifera.platform.net.sniffing.ISessionContext#getRealm()
	 * 
	 * @return The id of the current realm.
	 */
	long getRealm();
	
	/**
	 * Returns the id value of the space that tools and sniffing modules which 
	 * process this packet should create entities in.  In the user interface
	 * spaces are displayed as a collection of tabbed windows that the user
	 * can navigate.
	 * 
	 * @see com.netifera.platform.net.sniffing.IPacketContext#getSpaceId()
	 * 
	 * @return The id value for the space where new entities should be displayed.
	 */
	long getSpaceId();

}
