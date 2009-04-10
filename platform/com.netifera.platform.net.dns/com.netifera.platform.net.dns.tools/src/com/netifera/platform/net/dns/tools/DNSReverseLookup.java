package com.netifera.platform.net.dns.tools;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Type;

import com.netifera.platform.api.iterables.IndexedIterable;
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
import com.netifera.platform.util.addresses.AddressFormatException;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class DNSReverseLookup implements ITool {
	private static final boolean DEBUG = false;
	private static final int DEFAULT_DNS_INTERVAL = 200; // 200 milliseconds between requests
	private DNS dns;
	private IndexedIterable<InternetAddress> addresses;
	private INameResolver resolver;
	
	private IToolContext context;
	private long realm;

	private AtomicInteger activeRequests;
	private Queue<Runnable> retryQueue = new LinkedList<Runnable>();
	
	private int successCount = 0;

	
	public void toolRun(IToolContext context) throws ToolException {
		this.context = context;
		final int sendDelay = getSendDelay();
		// XXX hardcode local probe as realm
		IProbe probe = Activator.getInstance().getProbeManager().getLocalProbe();
		realm = probe.getEntity().getId();
		
		context.setTitle("Reverse lookup");
		
		setupToolOptions();

		context.setTitle("Reverse lookup "+addresses);
		
		try {
			if (dns != null) {
				resolver = dns.createNameResolver(Activator.getInstance().getSocketEngine());
			} else {
				resolver = Activator.getInstance().getNameResolver();
			}
			if (resolver == null) {
				throw new ToolException("No Name Resolver available on " + probe.getName());
			}
			
			activeRequests = new AtomicInteger(0);
			context.setTotalWork(addresses.itemCount());
			
			for (InternetAddress address: addresses) {
				if(DEBUG)
					context.debug("Looking up " + address + " outstanding = " + activeRequests.get());
				if (Thread.currentThread().isInterrupted())
					break;
				try {
					Thread.sleep(sendDelay);
				} catch(InterruptedException e) {
					context.warning("Interrupted");
					Thread.currentThread().interrupt();
					break;
				}
				reverseLookup(address, context);
			}
			
			while (activeRequests.get() > 0) {
				Runnable retry = nextRetryOrNull();
				while (retry != null && !(Thread.currentThread().isInterrupted())) {
					Thread.sleep(sendDelay * 2);
					retry.run();
					retry = nextRetryOrNull();
				}
				Thread.sleep(500);
			}
			
		} catch (IOException e) {
			context.exception("I/O Exception", e);
		} catch (InterruptedException e) {
			context.warning("Interrupted");
		} finally {
			try {
				if (dns != null)
					resolver.shutdown();
			} catch (IOException e) {
				context.exception("I/O Exception", e);
			};
			if (successCount == 0)
				context.error("No records found");
			context.done();
		}
	}
	
	private int getSendDelay() {
		final String property = System.getProperty("netifera.dns.delay");
		if(property == null) {
			return DEFAULT_DNS_INTERVAL;
		}
		try {
			final int delay = Integer.parseInt(property);
			if(delay < 0 || delay > 10000) {
				return DEFAULT_DNS_INTERVAL;
			}
			return delay;
		} catch(NumberFormatException e) {
			return DEFAULT_DNS_INTERVAL;
		}
	}
	private void reverseLookup(final InternetAddress address, final IToolContext toolContext) {
			Name name = ReverseMap.fromAddress(address.toBytes());
			
			final AsynchronousLookup lookup = new AsynchronousLookup(name, Type.PTR, DClass.IN);
			lookup.setResolver(resolver.getExtendedResolver());
			CompletionHandler<Record[],Void> handler = new CompletionHandler<Record[],Void>() {
				int retry = 0;
				public void cancelled(Void attachment) {
					activeRequests.decrementAndGet();
					context.warning("Reverse lookup of "+address+" was cancelled");
					toolContext.worked(1);
				}
				public void completed(Record[] result, final Void attachment) {
					if (lookup.getResult() == AsynchronousLookup.TRY_AGAIN && retry < 3) {
						retry = retry + 1;
						final CompletionHandler<Record[],Void> handler = this;
						enqueueRetry(new Runnable() {
							public void run() {
								context.info("Retrying reverse lookup of "+address+" ("+retry+")");
								lookup.run(attachment, handler);
							}
						});
						return;
					}
					if(DEBUG) {
						context.info("completed to " + address + " outstanding = " + activeRequests.get());
					}
					if (result == null) {
						toolContext.worked(1);
						activeRequests.decrementAndGet();
						context.error(address+" Reverse lookup failed: "+lookup.getErrorString());
						return;
					}
					for (Record record: result)
						processRecord(record, context);
					toolContext.worked(1);
					activeRequests.decrementAndGet();
				}
				public void failed(Throwable exc, Void attachment) {
					activeRequests.decrementAndGet();
					toolContext.worked(1);

					if(lookup.getResult() == AsynchronousLookup.HOST_NOT_FOUND) {
						return;
					}
					// TRY_AGAIN set for SERVFAIL, retry one time only
					if(lookup.getResult() == AsynchronousLookup.TRY_AGAIN && retry == 0) {
						retry++;
						final CompletionHandler<Record[], Void> handler = this;
						enqueueRetry(new Runnable() {
							public void run() {
								context.info("Retrying reverse lookup of "+address+" ("+retry+") on SERVFAIL");
								lookup.run(null, handler);

							}
						});
						return;
						
					}
					context.error("Reverse lookup of "+address+" failed: "+ lookup.getErrorString() );
					
					/* Test for SERVFAIL */
					if(lookup.getResult() != AsynchronousLookup.TRY_AGAIN) {
						context.exception("Unexpected exception in reverse lookup of " + address, exc);
					}
					
				}};
				
			activeRequests.incrementAndGet();
			lookup.run(null,handler);
	}

	private void processRecord(Record o, IToolContext context) {
		if (o instanceof PTRRecord) {
			PTRRecord ptr = (PTRRecord) o;
			if (ptr.getTarget().toString().endsWith(".arpa.")) {
				context.warning("Malformed PTR entry: " + ptr.toString());
				return;
			}
			String reverseName = ptr.getName().toString();
			if (!reverseName.endsWith(".arpa.")) {
				warnUnknownFormat(reverseName);
				return;
			}
			context.info(ptr.toString());
			try {
				InternetAddress address = InternetAddress.fromARPA(reverseName);
				Activator.getInstance().getDomainEntityFactory().createPTRRecord(realm, context.getSpaceId(), address, ptr.getTarget().toString());
				successCount += successCount + 1;
			} catch(AddressFormatException e) {
				warnUnknownFormat(reverseName);
			}
		}
	}
	
	private void warnUnknownFormat(String name) {
		context.warning("Unknown reverse address format: "+ name);
	}
	private void setupToolOptions() throws RequiredOptionMissingException {
		dns = (DNS) context.getConfiguration().get("dns");
		addresses = (IndexedIterable<InternetAddress>) context.getConfiguration().get("target");
	}
	
	private synchronized void enqueueRetry(Runnable runnable) {
		retryQueue.add(runnable);
	}
	
	private synchronized Runnable nextRetryOrNull() {
		return retryQueue.poll();
	}
}