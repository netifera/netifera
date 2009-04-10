package com.netifera.platform.net.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.util.HexaEncoding;
import com.netifera.platform.util.addresses.INetworkAddress;

public abstract class NetworkAddressEntity extends AbstractEntity {
	
	private static final long serialVersionUID = -5247936378391570611L;

	private final byte[] address;
	/* Store the address as a string for faster queries */
	private final String addressString;

	
	protected NetworkAddressEntity(String entityName, IWorkspace workspace, long realmId, byte[] address) {
		super(entityName, workspace, realmId);
		this.address = address.clone();
		this.addressString = getAddress().toString(); /* to normalize */
	}
	
	NetworkAddressEntity() {
		address = null;
		addressString = null;
	}
	
	public byte[] getData() {
		return address; // FIXME readonly
	}
	
	// TODO jdoc do not use getAddress().toString(), use getAddressString() instead
	public abstract INetworkAddress getAddress();
	
	public String getAddressString() {
		return addressString;
	}
	
	protected static String createQueryKey(String entityName, long realmId, byte[] address) {
		return entityName + ":" + realmId + ":" + HexaEncoding.bytes2hex(address);
	}
	
	@Override
	protected String generateQueryKey() {
		return createQueryKey(getTypeName(), getRealmId(), address);
	}
}
