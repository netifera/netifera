package com.netifera.platform.net.dns.internal.tools;

import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolProvider;
import com.netifera.platform.net.dns.tools.AddDomain;
import com.netifera.platform.net.dns.tools.AddEmailAddress;
import com.netifera.platform.net.dns.tools.AddHostByName;
import com.netifera.platform.net.dns.tools.DNSReverseLookup;
import com.netifera.platform.net.dns.tools.DNSZoneTransfer;
import com.netifera.platform.net.dns.tools.HostNamesBruteforcer;
import com.netifera.platform.net.dns.tools.MXLookup;
import com.netifera.platform.net.dns.tools.NSLookup;
import com.netifera.platform.net.dns.tools.NetOpGeoLocalizer;

public class ToolProvider implements IToolProvider {

	private final static String[] toolClassNames = { 
		AddHostByName.class.getName(),
		AddDomain.class.getName(),
		NSLookup.class.getName(),
		MXLookup.class.getName(),
		HostNamesBruteforcer.class.getName(),
		DNSZoneTransfer.class.getName(),
		DNSReverseLookup.class.getName(),
		NetOpGeoLocalizer.class.getName(),
		AddEmailAddress.class.getName()
	};
	
	public ITool createToolInstance(String className) {
		if(className.equals(AddHostByName.class.getName())) {
			return new AddHostByName();
		} else if(className.equals(AddDomain.class.getName())) {
			return new AddDomain();
		} else if(className.equals(NSLookup.class.getName())) {
			return new NSLookup();
		} else if(className.equals(MXLookup.class.getName())) {
			return new MXLookup();
		} else if(className.equals(HostNamesBruteforcer.class.getName())) {
			return new HostNamesBruteforcer();
		} else if (className.equals(DNSZoneTransfer.class.getName())) {
			return new DNSZoneTransfer();
		} else if (className.equals(DNSReverseLookup.class.getName())) {
			return new DNSReverseLookup();
		} else if (className.equals(NetOpGeoLocalizer.class.getName())) {
			return new NetOpGeoLocalizer();
		} else if (className.equals(AddEmailAddress.class.getName())) {
			return new AddEmailAddress();
		}
		return null;
	}

	public String[] getProvidedToolClassNames() {
		return toolClassNames;
	}
}
