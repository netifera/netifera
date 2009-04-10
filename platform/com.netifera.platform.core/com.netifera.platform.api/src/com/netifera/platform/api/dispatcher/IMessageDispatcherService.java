package com.netifera.platform.api.dispatcher;


/**
 * A message dispatcher service instance is a pair of message dispatcher instances.
 * 
 * In the kernel, the client dispatcher receives and proccesses messages sent by probes 
 * and the server dispatcher processes messages sent from the kernel to the probe.
 * 
 * In remote probes, the server dispatcher processses messages sent over the network to
 * the probe and the client dispatcher is currently unused.
 * 
 *
 */
public interface IMessageDispatcherService {

	IClientDispatcher getClientDispatcher();
	IServerDispatcher getServerDispatcher();

}
