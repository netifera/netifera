package com.netifera.platform.tasks;

/**
 * This interface describes methods that a tool might call to request 
 * information from a user.  A tool running locally that needs information
 * from the user would use an implementation which displays a dialog to 
 * the user, but a tool running on a remote probe will need to either fail
 * or send the request back to the netifera console.
 *
 */
public interface ITaskPrompter {
	
	/**
	 * Prompt the user for a string.
	 * @param message Prompt to display to the user.
	 * @return User input or null
	 */
	String askString(String message);
	
	/**
	 * Prompt the user for a password or passphrase. 
	 * @param message Prompt to display to the user.
	 * @return User input or null
	 */
	String askPassword(String message);

}
