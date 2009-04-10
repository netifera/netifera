package com.netifera.platform.net.dns.service.nameresolver;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.xbill.DNS.TextParseException;

import com.netifera.platform.net.dns.service.client.ExtendedResolver;
import com.netifera.platform.util.addresses.inet.InternetAddress;

// FIXME try to avoid to throw TextParseException to keep org.xbill.DNS internal
public interface INameResolver {
	InternetAddress getAddressByName(String name) throws UnknownHostException;
	List<InternetAddress> getAddressesByName(String name) throws UnknownHostException, TextParseException;
	String getNameByAddress(InternetAddress address) throws UnknownHostException;
	List<String> getNamesByAddress(InternetAddress address) throws UnknownHostException;
	List<String> getNameServersForDomain(String domain) throws TextParseException;
	
	ExtendedResolver getExtendedResolver();
	void shutdown() throws IOException;
}
