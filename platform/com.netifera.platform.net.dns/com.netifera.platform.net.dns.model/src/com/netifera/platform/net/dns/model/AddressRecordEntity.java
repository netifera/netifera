package com.netifera.platform.net.dns.model;

import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.net.model.InternetAddressEntity;

/* for A and AAAA records */
abstract class AddressRecordEntity extends DNSRecordEntity {
	
	private static final long serialVersionUID = 3566339986137902420L;
	
	private final String hostname;
	/* Store the address as a string for faster queries */
	private final String addressString;
	private final String fqdm;
	protected final IEntityReference address;
	
	protected AddressRecordEntity(String typeName, IWorkspace workspace, long realmId, IEntityReference domain, String hostname, IEntityReference address) {
		super(typeName, workspace, realmId, domain);
		this.hostname = hostname.trim();
		this.address = address.createClone();
		this.addressString = getAddressEntity().getAddressString();
		this.fqdm = createFQDM(hostname);
	}
	
	AddressRecordEntity() {
		this.hostname = null;
		this.address = null;
		this.fqdm = null;
		this.addressString = null;
	}
	private String createFQDM(String name) {
		if(name.endsWith(".")) return name;
		return name + "." + getDomain().getFQDM();
	}
	
	public String getHostName() {
		return hostname;
	}
	
	public final InternetAddressEntity getAddressEntity() {
		return (InternetAddressEntity)referenceToEntity(address);
	}
	
	public String getAddressString() {
		return addressString;
	}
	
	public String getFQDM() {
		return fqdm;
	}
	
	@Override
	protected String generateQueryKey() {
		return createQueryKey(getTypeName(), getRealmId(), addressString, fqdm);
	}
	
	protected static String createQueryKey(String typeName, long realmId, String address, String fqdm) {
		return typeName + ":" + realmId + ":" + address + ":" + fqdm;
	}
}
