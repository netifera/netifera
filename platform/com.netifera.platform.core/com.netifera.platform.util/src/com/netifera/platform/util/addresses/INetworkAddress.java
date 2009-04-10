package com.netifera.platform.util.addresses;

public interface INetworkAddress extends IAbstractAddress,
		Comparable<INetworkAddress> {
	/**
	 * The network family
	 */
	NetworkFamily getNetworkFamily();
}
