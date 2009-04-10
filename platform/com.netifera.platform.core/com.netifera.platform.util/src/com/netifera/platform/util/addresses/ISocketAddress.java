package com.netifera.platform.util.addresses;

@Deprecated // FIXME james || lee
//WRONG, inet socket addr is netaddr + port
public abstract interface ISocketAddress extends IAbstractAddress {
	// can be union:
	// - INetworkAddress
	// - IPathAddress

}
