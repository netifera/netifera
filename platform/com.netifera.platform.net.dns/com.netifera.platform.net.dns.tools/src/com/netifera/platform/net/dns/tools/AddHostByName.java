package com.netifera.platform.net.dns.tools;

import java.net.UnknownHostException;
import java.util.List;

import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolContext;
import com.netifera.platform.api.tools.ToolException;
import com.netifera.platform.net.dns.internal.tools.Activator;
import com.netifera.platform.net.dns.model.AAAARecordEntity;
import com.netifera.platform.net.dns.model.ARecordEntity;
import com.netifera.platform.net.dns.service.nameresolver.INameResolver;
import com.netifera.platform.net.model.HostEntity;
import com.netifera.platform.tools.RequiredOptionMissingException;
import com.netifera.platform.util.addresses.inet.IPv4Address;
import com.netifera.platform.util.addresses.inet.IPv6Address;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class AddHostByName implements ITool {
	
	private Name name;
	private INameResolver resolver;
	
	private IToolContext context;
	private long realm;

	public void toolRun(IToolContext context) throws ToolException {
		this.context = context;

		// XXX hardcode local probe as realm
		IProbe probe = Activator.getInstance().getProbeManager().getLocalProbe();
		realm = probe.getEntity().getId();
		
		setupToolOptions();

		context.setTitle("Resolve name "+name);

		try {
			List<String> nameServers = resolver.getNameServersForDomain(name.toString());
			if (nameServers.size()>0)
				Activator.getInstance().getDomainEntityFactory().createDomain(realm, context.getSpaceId(), name.toString());
			List<InternetAddress> addresses = resolver.getAddressesByName(name.toString());
			for (InternetAddress address: addresses) {
				context.info(name+" has address "+address);
	
				HostEntity entity = null;
				if (address instanceof IPv6Address) {
					AAAARecordEntity recordEntity = Activator.getInstance().getDomainEntityFactory().createAAAARecord(realm, context.getSpaceId(), name.toString(), (IPv6Address)address);
					entity = recordEntity.getAddressEntity().getHost();
				} else {
					ARecordEntity recordEntity = Activator.getInstance().getDomainEntityFactory().createARecord(realm, context.getSpaceId(), name.toString(), (IPv4Address)address);
					entity = recordEntity.getAddressEntity().getHost();
				}
				entity.addTag("Target");
				entity.update();
			}
		} catch (UnknownHostException e) {
			context.error("Unknown host: "+name);
		} catch (TextParseException e) {
			/* cannot happen */
		}
	}
	
	private void setupToolOptions() throws ToolException {
		resolver = Activator.getInstance().getNameResolver();
		if (resolver == null) {
			throw new ToolException("No Resolver service available");
		}
		String nameString = (String) context.getConfiguration().get("name");
		if (nameString == null || nameString.length() == 0) {
			throw new RequiredOptionMissingException("name");
		}
		if (nameString.endsWith(".")) {
			nameString = nameString.substring(0,nameString.length()-1);
		}
		try {
			name = new Name(nameString);
		} catch (TextParseException e) {
			throw new ToolException("Malformed host name: '"+nameString+"'", e);
		}
	}
}
