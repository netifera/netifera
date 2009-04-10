package com.netifera.platform.net.dns.model;

import com.netifera.platform.util.addresses.inet.IPv4Address;
import com.netifera.platform.util.addresses.inet.IPv6Address;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public interface IDomainEntityFactory {
	DomainEntity createDomain(long realm, long spaceId, String name);
		
	NSRecordEntity createNSRecord(long realm, long spaceId, String domain,
			String target);
	
	MXRecordEntity createMXRecord(long realm, long spaceId, String domain,
			String target, Integer priority);
	
	EmailAddressEntity createEmailAddress(long realm, long spaceId,
			String address);
	
	EmailAddressEntity createEmailAddress(long realm, long spaceId, String name,
			String address);
	
	ARecordEntity createARecord(long realm, long spaceId, String name,
			IPv4Address address);
	
	AAAARecordEntity createAAAARecord(long realm, long spaceId, String name,
			IPv6Address address);
	
	PTRRecordEntity createPTRRecord(long realm, long spaceId,
			InternetAddress address, String name);
}
