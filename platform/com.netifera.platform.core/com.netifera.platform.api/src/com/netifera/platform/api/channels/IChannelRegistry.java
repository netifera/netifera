package com.netifera.platform.api.channels;

public interface IChannelRegistry {
	/**
	 * Returns <tt>true</tt> if the specified channel type name is registered.
	 * This method must be called to be sure that a channel type exists in the
	 * registry before creating an <tt>IChannelConnecter</tt> with {@link #createConnecter(String)}
	 * or an <tt>IChannelServer</tt> with {@link #createServer(String)}.
	 * 
	 * @param channelType The name of the channel type to query.
	 * @return Returns <tt>true</tt> if the channel type is registered.
	 */
	boolean isChannelRegistered(String channelType);
	
	IChannelConnecter createConnecter(String config);
	
	IChannelServer createServer(String config);
}
