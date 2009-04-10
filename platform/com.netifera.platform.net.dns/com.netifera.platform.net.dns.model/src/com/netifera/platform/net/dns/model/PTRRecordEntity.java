package com.netifera.platform.net.dns.model;


import java.util.Locale;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.net.model.InternetAddressEntity;

public class PTRRecordEntity extends DNSRecordEntity {
	
	private static final long serialVersionUID = 5885077675573832948L;

	final public static String ENTITY_TYPE = "dns.ptr";

	final private IEntityReference address;
	/* Store the address as a string for faster queries */
	final private String addressString;
	final private String fqdm;
	final private String name;
	
	public PTRRecordEntity(IWorkspace workspace, long realmId, IEntityReference domain, IEntityReference address, String name) {
		super(ENTITY_TYPE, workspace, realmId, domain);
		this.address = address.createClone();
		this.addressString = getAddressEntity().getAddressString();
		this.name = name.toLowerCase(Locale.ENGLISH); // normalize?
		this.fqdm = createFQDM();
	}
	
	PTRRecordEntity() {
		address = null;
		addressString = null;
		fqdm = null;
		name = null;
	}
	private String createFQDM() {
		String name = getName();
		if (name.endsWith(".")) return name;
		return name+"."+getDomain().getFQDM();
	}
	public String getName() {
		return name;
	}
	
	public InternetAddressEntity getAddressEntity() {
		return (InternetAddressEntity)referenceToEntity(address);
	}
		
	public String getAddressString() {
		return addressString;
	}
	
	public String getFQDM() {
		return fqdm;
		
	}
	public static String createQueryKey(long realmId, String address, String fqdm) {
		return ENTITY_TYPE + ":" + realmId + ":" + address + ":" + fqdm;
	}
	
	@Override
	protected String generateQueryKey() {
		return createQueryKey(getRealmId(), addressString, fqdm);
	}
	
	@Override
	protected IEntity cloneEntity() {
		return new PTRRecordEntity(getWorkspace(), getRealmId(), domain, address, name);
	}
}
