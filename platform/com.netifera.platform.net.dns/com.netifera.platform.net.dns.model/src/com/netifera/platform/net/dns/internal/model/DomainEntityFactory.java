package com.netifera.platform.net.dns.internal.model;

import java.util.Locale;

import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.model.IWorkspaceEx;
import com.netifera.platform.net.dns.model.AAAARecordEntity;
import com.netifera.platform.net.dns.model.ARecordEntity;
import com.netifera.platform.net.dns.model.DNSRecordEntity;
import com.netifera.platform.net.dns.model.DomainEntity;
import com.netifera.platform.net.dns.model.EmailAddressEntity;
import com.netifera.platform.net.dns.model.IDomainEntityFactory;
import com.netifera.platform.net.dns.model.MXRecordEntity;
import com.netifera.platform.net.dns.model.NSRecordEntity;
import com.netifera.platform.net.dns.model.PTRRecordEntity;
import com.netifera.platform.net.model.HostEntity;
import com.netifera.platform.net.model.INetworkEntityFactory;
import com.netifera.platform.net.model.InternetAddressEntity;
import com.netifera.platform.util.addresses.inet.IPv4Address;
import com.netifera.platform.util.addresses.inet.IPv6Address;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.locators.TCPSocketLocator;

public class DomainEntityFactory implements IDomainEntityFactory {

	private IModelService model;
	private INetworkEntityFactory networkEntityFactory;
	
	protected void setModelService(IModelService model) {
		this.model = model;
	}

	protected void unsetModelService(IModelService model) {
		this.model = null;
	}
	
	protected void setNetworkEntityFactory(INetworkEntityFactory factory) {
		networkEntityFactory = factory;
	}

	protected void unsetNetworkEntityFactory(INetworkEntityFactory factory) {
		networkEntityFactory = null;
	}

	private String normalized(String fqdm) {
		if (fqdm.endsWith(".")) fqdm = fqdm.substring(0, fqdm.length()-1);
		return fqdm.toLowerCase(Locale.ENGLISH);
	}
	
	
	public synchronized DomainEntity createDomain(long realm, long spaceId, String fqdm) {
		fqdm = normalized(fqdm);
		DomainEntity domain = (DomainEntity) getWorkspace().findByKey(DomainEntity.createQueryKey(realm, fqdm));
		if(domain != null) {
			if(canAddDomainToSpace(domain))
				domain.addToSpace(spaceId);
			return domain;
		}
		
		// make sure to create the parent domain
		DomainEntity parent = null;
		int dotIndex = fqdm.indexOf('.');
		if (dotIndex >= 0)
			parent = createDomain(realm, spaceId, fqdm.substring(dotIndex+1));
		DomainEntity answer = new DomainEntity(getWorkspace(), realm, parent, fqdm);
		answer.save();
		if(canAddDomainToSpace(answer))
			answer.addToSpace(spaceId);
		return answer;
	}

	private boolean canAddDomainToSpace(DomainEntity e) {
		return e != null && !e.isTLD() && e.getParent() != null && e.getParent().isTLD();
	}
	private DomainEntity findDomain(long realm, String fqdm) {
		fqdm = normalized(fqdm);
		return (DomainEntity) getWorkspace().findByKey(DomainEntity.createQueryKey(realm, fqdm));
	}
	
	public synchronized NSRecordEntity createNSRecord(long realm, long spaceId, String domainName, String target) {
		domainName = normalized(domainName);
		DomainEntity domainEntity = createDomain(realm, spaceId, domainName);
		
		target = normalized(target);
		NSRecordEntity record = (NSRecordEntity) getWorkspace().findByKey(NSRecordEntity.createQueryKey(realm, target, domainEntity.getId()));
		if(record != null) {
			record.addToSpace(spaceId);
			return record;
		}
		
		record = new NSRecordEntity(getWorkspace(), realm, domainEntity.createReference(), target);
		record.save();
		record.addToSpace(spaceId); // FIXME if !arpa?
		return record;
	}

	public synchronized MXRecordEntity createMXRecord(long realm, long spaceId, String domainName, String target, Integer priority) {
		domainName = normalized(domainName);
		DomainEntity domainEntity = createDomain(realm, spaceId, domainName);
		
		target = normalized(target);
		MXRecordEntity record = (MXRecordEntity) getWorkspace().findByKey(MXRecordEntity.createQueryKey(realm, target, domainEntity.getId()));
		if(record != null) {
			record.addToSpace(spaceId);
			return record;
		}
		
		record = new MXRecordEntity(getWorkspace(), realm, domainEntity.createReference(), target, priority);
		record.save();
		record.addToSpace(spaceId);
		return record;
	}

	public synchronized EmailAddressEntity createEmailAddress(long realm, long spaceId, String address) {
		String accountName = address.substring(0, address.indexOf('@'));

		/*
		 * first check IP address literal (surrounded by square braces)
		 */
		if (address.contains("[")) {
			InternetAddressEntity addressEntity;
			String ip = address.substring(address.indexOf('[') + 1, address.indexOf(']'));
			InternetAddress inAddr = InternetAddress.fromString(ip);
			try {
				addressEntity = networkEntityFactory.createAddress(realm, spaceId, inAddr);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return null;
			}
			
			HostEntity hostEntity = addressEntity.getHost();
			if (hostEntity.getLabel() == null) { //XXX WTF?
				hostEntity.setLabel(ip);
				hostEntity.update();
			}

			TCPSocketLocator locator = new TCPSocketLocator(inAddr, 25);
			networkEntityFactory.createService(realm, spaceId, locator, "SMTP", null);

			// FIXME: no EmailAddressEntity created (because no StringDomain)
			
			return null;
		}
		
		/*
		 * if no IP address literal assume MX domain name.
		 * normalize domain name (but no accountname, see RFC 2821).
		 */
		String domainName = address.substring(address.indexOf('@') + 1).toLowerCase(Locale.ENGLISH);
		String normalizedAddress = accountName + "@" + domainName;

		EmailAddressEntity email = (EmailAddressEntity) getWorkspace().findByKey(EmailAddressEntity.createQueryKey(realm, normalizedAddress));
		if(email != null) {
			email.addToSpace(spaceId);
			return email;
		}
	
		DomainEntity domain = createDomain(realm, spaceId, domainName);
		
		email = new EmailAddressEntity(getWorkspace(), realm, normalizedAddress, domain.createReference());
//		email.addTag("@"+domainName);
		email.save();
		email.addToSpace(spaceId);
		return email;
	}

	public synchronized EmailAddressEntity createEmailAddress(long realm, long spaceId, String name, String address) {
		EmailAddressEntity entity = createEmailAddress(realm, spaceId, address);
		
		if (entity.getName() != null && entity.getName().equals(name))
			return entity; // no change
		
		entity.setName(name);
		entity.update();
		
		return entity;
	}
	
	public synchronized ARecordEntity createARecord(long realm, long spaceId, String name, IPv4Address address) {
		return (ARecordEntity)createAddressRecord(realm, spaceId, name, address);
	}
	
	public synchronized AAAARecordEntity createAAAARecord(long realm, long spaceId, String name, IPv6Address address) {
		return (AAAARecordEntity)createAddressRecord(realm, spaceId, name, address);
	}
	
	private synchronized DNSRecordEntity createAddressRecord(long realm, long spaceId, String fqdm, InternetAddress address) {
		final String finalName = normalized(fqdm);
		final String addressString = address.toString();

		DNSRecordEntity record;
		if (address instanceof IPv4Address) {
			record = (ARecordEntity) getWorkspace().findByKey(ARecordEntity.createQueryKey(realm, addressString, finalName));
		} else {
			record = (AAAARecordEntity) getWorkspace().findByKey(AAAARecordEntity.createQueryKey(realm, addressString, finalName));
		}
		if(record != null) {
			record.addToSpace(spaceId);
			return record;
		}

		// use spaceId=0 to avoid commiting to the space yet, we'll commit once the entity is tagged
		InternetAddressEntity addressEntity = networkEntityFactory.createAddress(realm, 0, address);
		addressEntity.addName(finalName);
		addressEntity.save();
		HostEntity hostEntity = addressEntity.getHost();
		if (hostEntity.getLabel() == null) { //just set the first name discovered
			hostEntity.setLabel(finalName+" ("+address+")");
		}
		
		DomainEntity domainEntity = findDomain(realm, finalName); // XXX eh? look for .name?
		if (domainEntity == null) {
			String domain = finalName.substring(finalName.indexOf('.')+1);
			domainEntity = createDomain(realm, spaceId, domain);
		}
		
		if (!domainEntity.isTLD()) {
			hostEntity.addTag(domainEntity.getLevel(2).getFQDM());
		} else {
			hostEntity.addTag(finalName);
		}
		
		hostEntity.update();
		hostEntity.addToSpace(spaceId);
		
		if (address instanceof IPv4Address) {
			record = new ARecordEntity(getWorkspace(), realm, domainEntity.createReference(), finalName, addressEntity.createReference());
		} else {
			record = new AAAARecordEntity(getWorkspace(), realm, domainEntity.createReference(), finalName, addressEntity.createReference());
		}
		record.save();
		record.addToSpace(spaceId);
		return record;
	}
	
	public synchronized PTRRecordEntity createPTRRecord(long realm, long spaceId, InternetAddress address, String fqdm) {
		fqdm = normalized(fqdm);;
		if (fqdm.endsWith(".arpa")) { // invalid PTR entry (mostly error in bind zone
			return null;
		}
		String addressString = address.toString();

		PTRRecordEntity ptr = (PTRRecordEntity) getWorkspace().findByKey(PTRRecordEntity.createQueryKey(realm, addressString, fqdm));
		if(ptr != null) {
			ptr.addToSpace(spaceId);
			return ptr;
		}
		// use spaceId=0 to avoid commiting to the space yet, we'll commit once the entity is tagged
		InternetAddressEntity addressEntity = networkEntityFactory.createAddress(realm, 0, address);
		addressEntity.addName(fqdm);
		addressEntity.save();
		HostEntity hostEntity = addressEntity.getHost();
		if (hostEntity.getLabel() == null) { //just set the first name discovered
			hostEntity.setLabel(fqdm+" ("+address+")");
		}
		DomainEntity domainEntity = findDomain(realm, fqdm);
		if (domainEntity == null) {
			String domain = fqdm.substring(fqdm.indexOf(".")+1);
			domainEntity = createDomain(realm, spaceId, domain);
		}

		if (!domainEntity.isTLD())
			hostEntity.addTag(domainEntity.getLevel(2).getFQDM());
		else
			hostEntity.addTag(fqdm);

		hostEntity.update();
		hostEntity.addToSpace(spaceId);
		
		ptr = new PTRRecordEntity(getWorkspace(), realm, domainEntity.createReference(), addressEntity.createReference(), fqdm);
		ptr.save();
		ptr.addToSpace(spaceId);
		return ptr;
	}
	
	private IWorkspaceEx getWorkspace() {
		if(model == null || model.getCurrentWorkspace() == null) {
			throw new IllegalStateException("Cannot create DNS entities because no workspace is currently open");
		}
		return (IWorkspaceEx) model.getCurrentWorkspace();
	}
	
}
