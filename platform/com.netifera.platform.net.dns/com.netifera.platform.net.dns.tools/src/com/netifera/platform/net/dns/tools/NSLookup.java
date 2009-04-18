package com.netifera.platform.net.dns.tools;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolContext;
import com.netifera.platform.api.tools.ToolException;
import com.netifera.platform.net.dns.internal.tools.Activator;
import com.netifera.platform.net.dns.model.MXRecordEntity;
import com.netifera.platform.net.dns.model.NSRecordEntity;
import com.netifera.platform.net.dns.service.DNS;
import com.netifera.platform.net.dns.service.nameresolver.INameResolver;
import com.netifera.platform.net.model.ServiceEntity;
import com.netifera.platform.tools.RequiredOptionMissingException;
import com.netifera.platform.util.addresses.inet.IPv4Address;
import com.netifera.platform.util.addresses.inet.IPv6Address;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.locators.TCPSocketLocator;
import com.netifera.platform.util.locators.UDPSocketLocator;

public class NSLookup implements ITool {
	
	private DNS dns;
	private Name domain;
	private INameResolver resolver;
	
	private IToolContext context;
	private long realm;

	public void toolRun(IToolContext context) throws ToolException {
		this.context = context;
		
		// XXX hardcode local probe as realm
		IProbe probe = Activator.getInstance().getProbeManager().getLocalProbe();
		realm = probe.getEntity().getId();
		
		setupToolOptions();

		context.setTitle("Lookup NS records for "+domain);
		
		try {
			if (dns != null)
				resolver = dns.createNameResolver(Activator.getInstance().getSocketEngine());
			else
				resolver = Activator.getInstance().getNameResolver();
			getNS();
		} catch (IOException e) {
			context.exception("I/O Exception", e);
		} finally {
			context.done();
		}
	}
	
	private boolean getNS() {
		context.setStatus("Querying for authoritative name servers");
		Lookup lookup = new Lookup(domain, Type.NS);
		lookup.setResolver(resolver.getExtendedResolver());
		lookup.setSearchPath((Name[])null);

		Record [] records = lookup.run();
		if (records == null) {
			context.info("No NS records found for "+domain);
			return false;
		}
		// if the domain has ns records, it exists
		Activator.getInstance().getDomainEntityFactory().createDomain(realm, context.getSpaceId(), domain.toString());
		for (Record record: records)
			processRecord(record);
		return true;
	}

	private void processRecord(Record o) {
		context.info(o.toString());
		if (o instanceof ARecord) {
			ARecord a = (ARecord) o;
			Activator.getInstance().getDomainEntityFactory().createARecord(realm, context.getSpaceId(), a.getName().toString(), IPv4Address.fromInetAddress(a.getAddress()));
		} else if (o instanceof AAAARecord) {
			AAAARecord aaaa = (AAAARecord) o;
			Activator.getInstance().getDomainEntityFactory().createAAAARecord(realm, context.getSpaceId(), aaaa.getName().toString(), IPv6Address.fromInetAddress(aaaa.getAddress()));
		} else if (o instanceof PTRRecord) {
			PTRRecord ptr = (PTRRecord) o;
//			Activator.getInstance().getDomainEntityFactory().createARecord(realm, a.getName().toString(), InternetAddress.fromInetAddress(o..getAddress()));
			context.warning("unhandled record: "+ptr);
		} else if (o instanceof MXRecord) {
			processMXRecord((MXRecord) o);
		} else if (o instanceof NSRecord) {
			processNSRecord((NSRecord) o);
		} else {
			context.warning("Unhandled DNS record: "+o);
		}
	}
	
	private void processNSRecord(NSRecord ns) {
		NSRecordEntity entity = Activator.getInstance().getDomainEntityFactory().createNSRecord(realm, context.getSpaceId(), domain.toString(), ns.getTarget().toString());
		try {
			List<InternetAddress> addresses = resolver.getAddressesByName(ns.getTarget().toString());
			for (InternetAddress address: addresses) {
				if (address instanceof IPv4Address) {
					Activator.getInstance().getDomainEntityFactory().createARecord(realm, context.getSpaceId(), ns.getTarget().toString(), (IPv4Address)address);
				} else {
					Activator.getInstance().getDomainEntityFactory().createAAAARecord(realm, context.getSpaceId(), ns.getTarget().toString(), (IPv6Address)address);
				}
				UDPSocketLocator locator = new UDPSocketLocator(address,53);
				ServiceEntity service = Activator.getInstance().getNetworkEntityFactory().createService(realm, context.getSpaceId(), locator, "DNS", null);
				entity.setService(service); //XXX just the last one will be kept as default service when accessing it from the NS record entity
			}
			entity.save();
		} catch (UnknownHostException e) {
			context.warning("Could not resolve NS record target "+ns.getTarget());
		} catch (TextParseException e) {
			context.warning("Malformed host name as NS record target: "+ns.getTarget());
		}
	}

	private void processMXRecord(MXRecord mx) {
		MXRecordEntity entity = Activator.getInstance().getDomainEntityFactory().createMXRecord(realm, context.getSpaceId(), domain.toString(), mx.getTarget().toString(), mx.getPriority());
		try {
			List<InternetAddress> addresses = resolver.getAddressesByName(mx.getTarget().toString());
			for (InternetAddress address: addresses) {
				if (address instanceof IPv4Address) {
					Activator.getInstance().getDomainEntityFactory().createARecord(realm, context.getSpaceId(), mx.getTarget().toString(), (IPv4Address)address);
				} else {
					Activator.getInstance().getDomainEntityFactory().createAAAARecord(realm, context.getSpaceId(), mx.getTarget().toString(), (IPv6Address)address);
				}
				TCPSocketLocator locator = new TCPSocketLocator(address,25);
				ServiceEntity service = Activator.getInstance().getNetworkEntityFactory().createService(realm, context.getSpaceId(), locator, "SMTP", null);
				entity.setService(service); //XXX just the last one will be kept as default service when accessing it from the MX record entity
			}
			entity.save();
		} catch (UnknownHostException e) {
			context.warning("Could not resolve MX record target "+mx.getTarget());
		} catch (TextParseException e) {
			context.warning("Malformed host name as MX record target: "+mx.getTarget());
		}
	}

	private void setupToolOptions() throws ToolException {
		dns = (DNS) context.getConfiguration().get("dns");
		String domainString = (String) context.getConfiguration().get("domain");
		if (domainString == null || domainString.length() == 0) {
			throw new RequiredOptionMissingException("domain");
		}
		if (domainString.endsWith(".")) {
			domainString = domainString.substring(0,domainString.length()-1);
		}
		try {
			domain = new Name(domainString);
		} catch (TextParseException e) {
			throw new ToolException("Malformed host name: '"+domainString+"'");
		}
	}
}