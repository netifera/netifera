package com.netifera.platform.api.daemon;

public interface IDaemon {
	/**
	 * Start this daemon.  If it is already started, do nothing.
	 */
	void start(long spaceId);
	
	/**
	 * Stop this daemon.  If it is not running, do nothing.
	 */
	void stop();
	
	/**
	 * Return <code>true</code> if the daemon is currently running.
	 * 
	 * @return The running state of the daemon.
	 */
	boolean isRunning();
}
