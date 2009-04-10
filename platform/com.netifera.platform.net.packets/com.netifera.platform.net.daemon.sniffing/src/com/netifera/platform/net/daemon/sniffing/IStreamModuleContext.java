package com.netifera.platform.net.daemon.sniffing;

import com.netifera.platform.net.sniffing.stream.ISessionKey;


/**
 * This is an extension of the <code>ISessionContext</code> interface which adds
 * a method to allow sniffing daemon modules to print output to the console in
 * the UI.
 */
public interface IStreamModuleContext extends IModuleContext {
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
	
	ISessionKey getKey();
}
