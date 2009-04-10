package com.netifera.platform.net.dns.model;

import java.util.Locale;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;

public class DomainEntity extends AbstractEntity implements Comparable<DomainEntity> {
	
	private static final long serialVersionUID = 3190898234021445159L;

	public static final String ENTITY_TYPE = "dns.domain";

	private final String fqdm;
	private final IEntityReference parent;
	
	public DomainEntity(IWorkspace workspace, long realmId, DomainEntity parent, String fqdm) {
		this(workspace, realmId, parent, fqdm, false);
	}
	
	private DomainEntity(IWorkspace workspace, long realmId, DomainEntity parent, String fqdm,
			boolean isTarget) { //, DomainEntity targetDomain) {
		super(ENTITY_TYPE, workspace, realmId);
		
		if (fqdm.endsWith(".")) {
			fqdm = fqdm.substring(0, fqdm.length()-1);
		}
		this.fqdm = fqdm.toLowerCase(Locale.ENGLISH);
		if (parent == null)
			this.parent = null;
		else
			this.parent = parent.createReference();
		
	}
	
	DomainEntity() {
		this.fqdm = null;
		this.parent  = null;
	}

	public DomainEntity getParent() {
		if (parent == null) return null;
		return (DomainEntity) referenceToEntity(parent);
	}
	
	public String getName() {
		int index = fqdm.indexOf('.');
		if (index == -1) return fqdm;
		return fqdm.substring(0, index);
	}

	public final String getFQDM() {
		return fqdm;
	}
	
	public boolean isTLD() {
		DomainEntity parent = getParent();
		if (parent == null) return true;
		if (parent.getParent() == null && parent.getName().length() <= 2 && getName().length() <= 3) return true;
		return false;
	}

	public int getLevel() {
		if (isTLD()) return 0;
		DomainEntity parent = getParent();
		if (parent.isTLD()) return 1;
		return 1+parent.getLevel();
	}
	
	public DomainEntity getLevel(int n) {
		if (n < 0) throw new IllegalArgumentException("Domain level must be positive");
		return (n >= getLevel()) ? this : getParent().getLevel(n);
	}
	
	@Override
	protected IEntity cloneEntity() {
		return new DomainEntity(getWorkspace(), getRealmId(), getParent(), fqdm);
	}
	
	public static String createQueryKey(long realmId, String fqdm) { 
		return ENTITY_TYPE + ":" + realmId + ":" + fqdm ; 
	}
	@Override
	protected String generateQueryKey() {
		return createQueryKey(getRealmId(), fqdm); 
		
	}
/*	public Iterable<INetworkServiceLocator> getNameServers() {
		List<INetworkServiceLocator> answer = new ArrayList<INetworkServiceLocator>();
		for (IEntity e: entity.findChildren(NSRecordEntity.typeName)) {
			NSRecordEntity ns = new NSRecordEntity((Entity)e);
			INetworkServiceLocator locator = ns.getLocator();
			if (locator != null) answer.add(locator);
		}
		return answer;
	}
*/
	
/*	public Iterable<INetworkServiceLocator> getMailExchangers() {
		List<INetworkServiceLocator> answer = new ArrayList<INetworkServiceLocator>();
		for (IEntity e: entity.findChildren("dns.mx")) {
			MXRecordEntity ns = new MXRecordEntity((Entity)e);
			INetworkServiceLocator locator = mx.getLocator();
			if (locator != null) answer.add(locator);
		}
		return answer;
	}*/

	public int compareTo(DomainEntity other) {
		return fqdm.compareTo(other.fqdm);
	}
}
