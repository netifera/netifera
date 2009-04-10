package com.netifera.platform.api.channels;

import com.netifera.platform.api.dispatcher.IMessenger;



/**
 * Callback interface for reporting connection progress and completion.
 */
public interface IChannelConnectProgress {
	
	/**
	 * Called on connection failure.
	 * @param reason Description of the reason the connection could not be completed.
	 * @param exception Optional exception which is the root cause of the connection failure. May be null.
	 */
	public void connectFailed(String reason, Throwable exception);
	
	/**
	 * Called when connection completes successfully.
	 */
	public void connectCompleted(IMessenger channelMessenger);
	
	/**
	 * Called when connection progresses to a new stage.  Complex
	 * channels with multi-stage connections can use this to report
	 * the current state of the connection attempt.
	 * @param update Textual description of current connection state.
	 */
	public void connectUpdate(String update);
	

}
