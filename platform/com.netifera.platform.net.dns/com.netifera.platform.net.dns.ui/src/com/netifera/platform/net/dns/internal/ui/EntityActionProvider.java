package com.netifera.platform.net.dns.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.iterables.IndexedIterable;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.net.dns.model.DomainEntity;
import com.netifera.platform.net.dns.model.NSRecordEntity;
import com.netifera.platform.net.dns.service.DNS;
import com.netifera.platform.net.dns.tools.DNSReverseLookup;
import com.netifera.platform.net.dns.tools.DNSZoneTransfer;
import com.netifera.platform.net.dns.tools.HostNamesBruteforcer;
import com.netifera.platform.net.dns.tools.MXLookup;
import com.netifera.platform.net.dns.tools.NSLookup;
import com.netifera.platform.net.dns.tools.NetOpGeoLocalizer;
import com.netifera.platform.net.model.HostEntity;
import com.netifera.platform.net.model.NetblockEntity;
import com.netifera.platform.net.model.ServiceEntity;
import com.netifera.platform.tools.options.GenericOption;
import com.netifera.platform.tools.options.IterableOption;
import com.netifera.platform.tools.options.StringOption;
import com.netifera.platform.ui.actions.ToolAction;
import com.netifera.platform.ui.api.actions.IEntityActionProvider;
import com.netifera.platform.util.addresses.inet.IPv4Address;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class EntityActionProvider implements IEntityActionProvider {

	public List<IAction> getActions(IShadowEntity entity) {
		List<IAction> answer = new ArrayList<IAction>();
		
		if (entity instanceof DomainEntity) {
			String domain = ((DomainEntity)entity).getFQDM();
			
			ToolAction nsLookup = new ToolAction("Lookup NS records for "+domain, NSLookup.class.getName());
			nsLookup.addFixedOption(new StringOption("domain", "Domain", "Target domain", domain));
			nsLookup.addOption(new GenericOption(DNS.class, "dns", "Name Server", "Target Name Server", null));
			answer.add(nsLookup);

			ToolAction mxLookup = new ToolAction("Lookup MX records for "+domain, MXLookup.class.getName());
			mxLookup.addFixedOption(new StringOption("domain", "Domain", "Target domain", domain));
			mxLookup.addOption(new GenericOption(DNS.class, "dns", "Name Server", "Target Name Server", null));
			answer.add(mxLookup);

			ToolAction hostNamesBruteforcer = new ToolAction("Lookup Common Host Names *."+domain, HostNamesBruteforcer.class.getName());
			hostNamesBruteforcer.addFixedOption(new StringOption("domain", "Domain", "Target domain", domain));
			hostNamesBruteforcer.addOption(new GenericOption(DNS.class, "dns", "Name Server", "Target Name Server", null));
			answer.add(hostNamesBruteforcer);
		}

		DNS dns = (DNS) entity.getAdapter(DNS.class);
		if (dns != null) {
			ToolAction zoneTransfer = new ToolAction("Request Zone Transfer", DNSZoneTransfer.class.getName());
			zoneTransfer.addOption(new StringOption("domain", "Domain", "Target domain", ""));
			zoneTransfer.addFixedOption(new GenericOption(DNS.class,"dns", "Name Server", "Target Name Server", dns));
			answer.add(zoneTransfer);
		}

		if (entity instanceof NSRecordEntity) {
			ServiceEntity service = ((NSRecordEntity)entity).getService();
			if (service != null) {
				dns = (DNS) service.getAdapter(DNS.class);
				ToolAction zoneTransfer = new ToolAction("Request Zone Transfer", DNSZoneTransfer.class.getName());
				zoneTransfer.addOption(new StringOption("domain", "Domain", "Target domain", ((NSRecordEntity)entity).getDomain().getFQDM()));
				zoneTransfer.addFixedOption(new GenericOption(DNS.class,"dns", "Name Server", "Target Name Server", dns));
				answer.add(zoneTransfer);			
			}
		}
		
		IndexedIterable<InternetAddress> addresses = getInternetAddressIndexedIterable(entity);
		if (addresses != null) {
			ToolAction reverseLookup = new ToolAction("Reverse DNS Lookup", DNSReverseLookup.class.getName());
			reverseLookup.addFixedOption(new IterableOption(InternetAddress.class, "target", "Target", "Addresses to reverse-lookup", addresses));
			reverseLookup.addOption(new GenericOption(DNS.class, "dns", "Name Server", "Target Name Server", dns));
			answer.add(reverseLookup);

			addresses = getIPv4AddressIndexedIterable(entity);
			if (addresses != null && !(entity instanceof NetblockEntity) && (!(entity instanceof HostEntity) || ((HostEntity)entity).getDefaultAddress().getNamedAttribute("country") == null)) {
				if (addresses.itemCount() > 1 || (addresses.itemAt(0).isUniCast() && !addresses.itemAt(0).isPrivate())) {
					ToolAction geoLocalizer = new ToolAction("Lookup Country by Address", NetOpGeoLocalizer.class.getName());
					geoLocalizer.addFixedOption(new IterableOption(InternetAddress.class, "target", "Target", "Addresses to geo-localize", addresses));
					answer.add(geoLocalizer);
				}
			}
		}
		return answer;
	}

	public List<IAction> getQuickActions(IShadowEntity shadow) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	private IndexedIterable<InternetAddress> getInternetAddressIndexedIterable(IEntity entity) {
		return (IndexedIterable<InternetAddress>) entity.getIterableAdapter(InternetAddress.class);
	}

	@SuppressWarnings("unchecked")
	private IndexedIterable<InternetAddress> getIPv4AddressIndexedIterable(IEntity entity) {
		return (IndexedIterable<InternetAddress>) entity.getIterableAdapter(IPv4Address.class);
	}
}
