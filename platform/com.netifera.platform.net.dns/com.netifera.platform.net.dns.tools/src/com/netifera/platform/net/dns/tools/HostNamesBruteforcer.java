package com.netifera.platform.net.dns.tools;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolContext;
import com.netifera.platform.api.tools.ToolException;
import com.netifera.platform.net.dns.internal.tools.Activator;
import com.netifera.platform.net.dns.service.DNS;
import com.netifera.platform.net.dns.service.client.AsynchronousLookup;
import com.netifera.platform.net.dns.service.nameresolver.INameResolver;
import com.netifera.platform.net.sockets.CompletionHandler;
import com.netifera.platform.tools.RequiredOptionMissingException;
import com.netifera.platform.util.addresses.inet.IPv4Address;
import com.netifera.platform.util.addresses.inet.IPv6Address;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class HostNamesBruteforcer implements ITool {
	private final static boolean DEBUG = false;
	private DNS dns;
	private Name domain;
	private INameResolver resolver;
	
	private IToolContext context;
	private long realm;

	private InternetAddress ignoreAddress = null;

	private AtomicInteger activeRequests;
	
	private static String[] commonNames = {"www", "www2", "web", "ssl", "static", "main", "home", "go",
			"ftp", "image", "images", "photo", "photos", "img", "pictures", "search",
			"mail", "webmail", "email", "mymail", "mx", "snmp", "pop", "pop3", "imap", "exchange",
			"ns", "dns", "mdns", "nameserver", "fw", "firewall", "router",
			"register", "login", "id", "passport",
			"vpn", "proxy", "cache", "upload", "download",
			"private", "partner", "partners", "customer", "customers", "member", "members", "user", "users",
			"b2b", "erp",
			"global", "us", "mx", "fr", "it", "usa", "de", "uk", "ca", "tw", "cn", "kr", "jp", "hk", "sg", "th", "my", "is", "in",
			"forum", "forums", "bbs", "blog", "blogs", "weblog", "weblogs", "wiki", "media", "video", "videos", "movie", "movies", "music", "tv", "community",
			"support", "network", "admin", "security", "secure", "sec", "manager", "manage", "management",
			"news", "feeds", "service", "sevices", "game", "games", "help", "list", "lists", "archive", "archives",
			"file", "files", "database", "data", "db", "oracle", "sql", "mysql", "cvs", "svn", "irc", "dc", "domain", "update",
			"buy", "sell", "sells", "pay", "payment", "payments", "shop", "shopping", "store", "webstore", "products", "product", "order", "orders", "report", "reports", "test",
			"research", "job", "jobs", "careers",
			"sms", "mobile", "phone",
			"stats", "localhost"
	};
	
	public void toolRun(IToolContext context) throws ToolException {
		this.context = context;

		// XXX hardcode local probe as realm
		IProbe probe = Activator.getInstance().getProbeManager().getLocalProbe();
		realm = probe.getEntity().getId();
		
		setupToolOptions();

		context.setTitle("Bruteforce host names *."+domain);
		context.setTotalWork(commonNames.length);

		if (dns != null)
			try {
				resolver = dns.createNameResolver(Activator.getInstance().getSocketEngine());
			} catch (IOException e) {
				context.exception("I/O Exception", e);
				context.done();
				return;
			}
		else
			resolver = Activator.getInstance().getNameResolver();

		context.setStatus("Get authoritative name servers");
		
		if (!getNS()) {
			context.error("Authoritative name servers not found, "+domain+" might not be a real domain");
			context.done();
			return;
		}

		context.setStatus("Lookup *."+domain);

		try {

			try {
				ignoreAddress = resolver.getAddressByName("kjhakjsd."+domain.toString());
			} catch (UnknownHostException e) {
			}

			if (ignoreAddress != null)
				context.warning("The DNS server resolves non-existent names");

			activeRequests = new AtomicInteger(0);
			resolveHostName(domain.toString());
			for (String each : commonNames) {
				resolveHostName(each + "." + domain.toString());
				Thread.sleep(100);
			}
			
			while (activeRequests.get() > 0) {
				if(DEBUG) {
					context.debug("activeRequests = "+activeRequests.get());
				}
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			context.warning("Interrupted");
		} finally {
			try {
				if (dns != null)
					resolver.shutdown();
			} catch (IOException e) {
				context.exception("I/O Exception", e);
			};
			context.done();
		}
	}

	// TODO only bruteforcing Type.A, what about Type.AAAA?
	private void resolveHostName(final String fqdm) {
		try {
			final AsynchronousLookup lookup = new AsynchronousLookup(fqdm);
			lookup.setResolver(resolver.getExtendedResolver());
			CompletionHandler<Record[],Void> handler = new CompletionHandler<Record[],Void>() {
				int retries = 0;
				public void cancelled(Void attachment) {
					activeRequests.decrementAndGet();
					context.warning(fqdm + " lookup cancelled");
					context.worked(1);
				}
				public void completed(Record[] result, Void attachment) {
					if (lookup.getResult() == AsynchronousLookup.TRY_AGAIN && retries < 3) {
						retries = retries + 1;
						context.debug("Retrying: "+fqdm+" ("+retries+")");
						lookup.run(attachment, this);
						return;
					}
					activeRequests.decrementAndGet();
					context.worked(1);
					if (result == null) {
						context.error(fqdm+" lookup failed: "+lookup.getErrorString());
						return;
					}
					for (Record record: result)
						processRecord(record);
				}
				public void failed(Throwable exc, Void attachment) {
					activeRequests.decrementAndGet();
					context.worked(1);
					if(lookup.getResult() == AsynchronousLookup.HOST_NOT_FOUND ||
							lookup.getResult() == AsynchronousLookup.TYPE_NOT_FOUND) {
						return;
					}
					if(exc instanceof SocketTimeoutException) {
						context.warning("Timeout looking up " + fqdm);
						return;
					}
					context.exception(fqdm+" lookup failed", exc);
				}};
				
			activeRequests.incrementAndGet();
			lookup.run(null,handler);
		} catch (TextParseException e) {
			context.warning("Malformed host name: " + fqdm);
		}
	}

	private void processRecord(Record record) {
		if (record instanceof ARecord) {
			IPv4Address addr = IPv4Address.fromInetAddress(((ARecord)record).getAddress());
			if (addr != null) {
				if (ignoreAddress == null || !ignoreAddress.equals(addr)) {
					context.info(record.toString());
					Activator.getInstance().getDomainEntityFactory().createARecord(realm, context.getSpaceId(), ((ARecord)record).getName().toString(), addr);
				}
			}
		} else if (record instanceof AAAARecord) {
			IPv6Address addr = IPv6Address.fromInetAddress(((AAAARecord)record).getAddress());
			if (addr != null) {
				if (ignoreAddress == null || !ignoreAddress.equals(addr)) {
					context.info(record.toString());
					Activator.getInstance().getDomainEntityFactory().createAAAARecord(realm, context.getSpaceId(), ((AAAARecord)record).getName().toString(), addr);
				}
			}
		}
	}
	
	private boolean getNS() {
		Lookup lookup = new Lookup(domain, Type.NS);
		lookup.setResolver(resolver.getExtendedResolver());
		lookup.setSearchPath((Name[])null);

		Record[] records = lookup.run();
		if (records == null) {
			context.info("No NS records found for " + domain);
			return false;
		}
		// if the domain has ns records, it exists
		Activator.getInstance().getDomainEntityFactory().createDomain(realm, context.getSpaceId(), domain.toString());
		for (int i = 0; i < records.length; i++) {
			NSRecord ns = (NSRecord) records[i];
			Activator.getInstance().getDomainEntityFactory().createNSRecord(realm, context.getSpaceId(), domain.toString(), ns.getTarget().toString());
		}
		return true;
	}

	private void setupToolOptions() throws ToolException {
		dns = (DNS) context.getConfiguration().get("dns");
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
