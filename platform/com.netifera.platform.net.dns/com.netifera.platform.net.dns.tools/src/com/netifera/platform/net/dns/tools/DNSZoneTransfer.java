package com.netifera.platform.net.dns.tools;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.ZoneTransferException;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolContext;
import com.netifera.platform.api.tools.ToolException;
import com.netifera.platform.net.dns.internal.tools.Activator;
import com.netifera.platform.net.dns.service.DNS;
import com.netifera.platform.tools.RequiredOptionMissingException;
import com.netifera.platform.util.addresses.inet.IPv4Address;
import com.netifera.platform.util.addresses.inet.IPv6Address;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.patternmatching.HostnameMatcher;

public class DNSZoneTransfer implements ITool {

	private DNS dns;
	private Name domain;
	private IToolContext context;
	private long realm;

	public void toolRun(IToolContext context) throws ToolException {
		this.context = context;
		
		// XXX hardcode local probe as realm
		IProbe probe = Activator.getInstance().getProbeManager().getLocalProbe();
		realm = probe.getEntity().getId();
		
		context.setTitle("Zone transfer");
		
		setupToolOptions();
		
		context.setTitle("Zone transfer of "+domain+" from "+dns.getLocator());
		
		try {
//			resolver = dns.createResolver(Activator.getInstance().getSocketEngine());
			transferZone();
		} catch (ConnectException e) {
			context.error("Cannot connect to "+dns);
		} catch (SocketTimeoutException e) {
			context.error("Connection to "+dns+" timed out");
		} catch (ZoneTransferException e) {
			context.error(dns+" doesnt allow zone transfer of "+domain);
		} catch (IOException e) {
			context.exception("I/O Exception", e);
		} finally {
			context.done();
		}
	}
		
	// Transfer a zone from a server and print it
	private void transferZone() throws IOException, ZoneTransferException {
		for (Object o: dns.zoneTransfer(domain.toString())) {
			if (o instanceof ARecord) {
				ARecord a = (ARecord) o;
				Activator.getInstance().getDomainEntityFactory().createARecord(realm, context.getSpaceId(), a.getName().toString(), IPv4Address.fromInetAddress(a.getAddress()));
			} else if (o instanceof AAAARecord) {
				AAAARecord aaaa = (AAAARecord) o;
				Activator.getInstance().getDomainEntityFactory().createAAAARecord(realm, context.getSpaceId(), aaaa.getName().toString(), IPv6Address.fromInetAddress(aaaa.getAddress()));
			} else if (o instanceof PTRRecord) {
				PTRRecord ptr = (PTRRecord) o;
				String reverseName = ptr.getName().toString();
				if (!reverseName.endsWith(".in-addr.arpa.")) {
					context.error("Unknown reverse address format: "+reverseName);
					continue;
				}
				String[] octets = reverseName.split("\\.");
				InternetAddress address = InternetAddress.fromString(octets[3]+"."+octets[2]+"."+octets[1]+"."+octets[0]); // XXX ipv6
				String hostname = ptr.getTarget().toString();
				/* verify the hostname is valid before adding it to model
				 * (avoid configuration errors to pollute the model) */
				if (HostnameMatcher.matches(hostname)) {
					Activator.getInstance().getDomainEntityFactory().createPTRRecord(realm, context.getSpaceId(), address, ptr.getTarget().toString());
				}
			} else if (o instanceof MXRecord) {
				MXRecord mx = (MXRecord) o;
				Activator.getInstance().getDomainEntityFactory().createMXRecord(realm, context.getSpaceId(), domain.toString(), mx.getTarget().toString(), mx.getPriority());
			} else if (o instanceof NSRecord) {
				NSRecord ns = (NSRecord) o;
				Activator.getInstance().getDomainEntityFactory().createNSRecord(realm, context.getSpaceId(), domain.toString(), ns.getTarget().toString());
			} else {
				context.warning("Unhandled DNS record: "+o);
			}
			if (Thread.currentThread().isInterrupted()) {
				context.warning("Interrupted");
				return;
			}
		}
	}
	
	private void setupToolOptions() throws ToolException {
		dns = (DNS) context.getConfiguration().get("dns");
		if (dns == null)
			throw new RequiredOptionMissingException("dns");
		
		String domainString = (String) context.getConfiguration().get("domain");
		if (domainString == null || domainString.length() == 0)
			throw new RequiredOptionMissingException("domain");
		if (domainString.endsWith(".")) {
			domainString = domainString.substring(0, domainString.length()-1);
		}
		try {
			domain = new Name(domainString);
		} catch (TextParseException e) {
			throw new ToolException("Malformed domain name: '"+domainString+"'", e);
		}
	}
}
