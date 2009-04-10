package com.netifera.platform.net.dns.service.nameresolver;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.netifera.platform.net.dns.service.client.ExtendedResolver;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class NameResolver implements INameResolver {

	protected ExtendedResolver resolver;
	
	protected NameResolver() {}
	
	public NameResolver(ExtendedResolver resolver) {
		this.resolver = resolver;
	}
	
	public InternetAddress getAddressByName(String name) throws UnknownHostException {
		Lookup lookup;
		try {
			lookup = new Lookup(name);
		} catch (TextParseException e) {
			throw new UnknownHostException("Malformed host name: "+ name);
		}
		lookup.setResolver(resolver);
		lookup.setSearchPath((Name[])null);

		Record[] records = lookup.run();
		InternetAddress answer = null;
		if(records != null)
			for (Record record: records)
				if (record instanceof ARecord)
					return InternetAddress.fromInetAddress(((ARecord)record).getAddress());
				else if (record instanceof AAAARecord)
					answer = InternetAddress.fromInetAddress(((AAAARecord)record).getAddress());
		if (answer != null) return answer;
		throw new UnknownHostException(name);
	}

	public List<InternetAddress> getAddressesByName(String name) throws TextParseException, UnknownHostException {
		List<InternetAddress> answer = new ArrayList<InternetAddress>();
		
		getAddressesByName(answer, Type.A, name);
		getAddressesByName(answer, Type.AAAA, name);
		
		if (answer.isEmpty()) {
			throw new UnknownHostException(name);
		}
		
		return answer;
	}
	
	private void getAddressesByName(List<InternetAddress> list, int type, String name) throws TextParseException {
		Lookup lookup = new Lookup(name, type, DClass.IN);
		lookup.setResolver(resolver);
		lookup.setSearchPath((Name[])null);

		Record[] records = lookup.run();
		if (records != null) {
			for (Record record: records) {
				if (record instanceof ARecord)
					list.add(InternetAddress.fromInetAddress(((ARecord)record).getAddress()));
				else if (record instanceof AAAARecord)
					list.add(InternetAddress.fromInetAddress(((AAAARecord)record).getAddress()));
			}
		}
	}
	
	public String getNameByAddress(InternetAddress address) throws UnknownHostException {
		Name name = ReverseMap.fromAddress(address.toString());
		Lookup lookup = new Lookup(name, Type.PTR, DClass.IN);
		lookup.setResolver(resolver);
		lookup.setSearchPath((Name[])null);

		Record[] records = lookup.run();
		if(records != null)
			for (Record record: records)
				if (record instanceof PTRRecord)
					return ((PTRRecord)record).getName().toString();
		throw new UnknownHostException(address.toString());
	}

	public List<String> getNamesByAddress(InternetAddress address) throws UnknownHostException {
		Name name = ReverseMap.fromAddress(address.toString());
		Lookup lookup = new Lookup(name, Type.PTR, DClass.IN);
		lookup.setResolver(resolver);
		lookup.setSearchPath((Name[])null);

		Record [] records = lookup.run();
		if (records == null)
			return Collections.emptyList();
		List<String> answer = new ArrayList<String>();
		for (Record record: records)
			if (record instanceof PTRRecord)
				answer.add(((PTRRecord)record).getName().toString());
		return answer;
	}

	public List<String> getNameServersForDomain(String domain) throws TextParseException {
		Lookup lookup = new Lookup(Name.fromString(domain), Type.NS);
		lookup.setResolver(resolver);
		lookup.setSearchPath((Name[])null);
		Record [] records = lookup.run();
		List<String> answer = new ArrayList<String>();
		if (records != null)
			for (Record record: records)
				if (record instanceof NSRecord)
					answer.add(((NSRecord)record).getTarget().toString());
		return answer;
	}
	
	public ExtendedResolver getExtendedResolver() {
		return resolver;
	}
	
	public void shutdown() throws IOException {
		resolver.shutdown();
	}
}
