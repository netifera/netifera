package com.netifera.platform.net.dns.internal.sniffing;

import java.io.IOException;

import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;

import com.netifera.platform.net.daemon.sniffing.IIPSniffer;
import com.netifera.platform.net.daemon.sniffing.IPacketModuleContext;
import com.netifera.platform.net.packets.tcpip.IP;
import com.netifera.platform.net.packets.tcpip.IPv4;
import com.netifera.platform.net.packets.tcpip.IPv6;
import com.netifera.platform.net.packets.tcpip.UDP;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.util.addresses.AddressFormatException;
import com.netifera.platform.util.addresses.inet.IPv4Address;
import com.netifera.platform.util.addresses.inet.IPv6Address;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class DNSSniffer implements IIPSniffer {

	private final boolean DEBUG = System.getProperty("dnssniffer.debug") != null;
	
	public IPacketFilter getFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "DNS Sniffer";
	}

	public void handleIPv4Packet(IPv4 ipv4, IPacketModuleContext context) {
		handleIPPacket(ipv4, context);
	}
	
	public void handleIPv6Packet(IPv6 ipv6, IPacketModuleContext context) {
		handleIPPacket(ipv6, context);
	}
	
	private void handleIPPacket(IP ip, IPacketModuleContext context) {
		if (!(ip.getNextHeader() instanceof UDP))
			return;
		UDP udp = (UDP) ip.getNextHeader();
		if (udp.getSourcePort() != 53)
				return;

		try {
			Message msg = new Message(udp.payload().toBytes());
//			System.err.println(msg);
			
			int rcode = msg.getHeader().getRcode();
			if (rcode != Rcode.NOERROR && rcode != Rcode.NXDOMAIN)
				return; // no good response
			
//			processRecords(msg.getSectionArray(0), context.getRealm());
			processRecords(msg.getSectionArray(1), context);
//			processRecords(msg.getSectionArray(2), context.getRealm());
//			processRecords(msg.getSectionArray(3), context.getRealm());
		} catch (IOException e) {
			// FIXME if (DEBUG)
			context.printOutput('(' + getName() + ") I/O Error: " + e.getMessage() + " for " + udp);
		}
	}
	
	private void processRecords(Record[] records, IPacketModuleContext context) {
		final long realm = context.getRealm();
		final long space = context.getSpaceId();
		for (Record o: records) {
			if (o instanceof ARecord) {
				ARecord a = (ARecord) o;
				Activator.getInstance().getDomainEntityFactory().createARecord(realm, space, a.getName().toString(), IPv4Address.fromInetAddress(a.getAddress()));
			} else if (o instanceof AAAARecord) {
					AAAARecord aaaa = (AAAARecord) o;
					Activator.getInstance().getDomainEntityFactory().createAAAARecord(realm, space, aaaa.getName().toString(), IPv6Address.fromInetAddress(aaaa.getAddress()));
			} else if (o instanceof PTRRecord) {
				PTRRecord ptr = (PTRRecord) o;
				String reverseName = ptr.getName().toString();
				if (!reverseName.endsWith(".arpa.")) {
					warnUnknownFormat(context, reverseName);
					continue;
				}
				try {
					InternetAddress address = InternetAddress.fromARPA(reverseName);
					Activator.getInstance().getDomainEntityFactory().createPTRRecord(realm, space, address, ptr.getTarget().toString());
				} catch(AddressFormatException e) {
					context.printOutput("Could not parse record " + reverseName);
				}
			} else if (o instanceof MXRecord) {
				if (DEBUG) context.printOutput("Unhandled DNS record: "+o);
				//MXRecord mx = (MXRecord) o;
				//Activator.getInstance().getDomainEntityFactory().createMXRecord(realm, domain.toString(), mx.getTarget().toString(), mx.getPriority());
			} else if (o instanceof NSRecord) {
				if (DEBUG) context.printOutput("Unhandled DNS record: "+o);
				//NSRecord ns = (NSRecord) o;
				//Activator.getInstance().getDomainEntityFactory().createNSRecord(realm, domain.toString(), ns.getTarget().toString());
			} else if (o instanceof CNAMERecord) {
				if (DEBUG) context.printOutput("Unhandled DNS record: "+o);
			} else {
				// FIXME should be display to dev people only
				context.printOutput("Unhandled DNS record: "+o);
			}
		}
	}
	
	private void warnUnknownFormat(IPacketModuleContext context, String name) {
		context.printOutput("Unknown reverse address format: "+ name);
	}
}
