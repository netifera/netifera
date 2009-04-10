package com.netifera.platform.net.http.tools;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.xbill.DNS.TextParseException;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolContext;
import com.netifera.platform.api.tools.ToolException;
import com.netifera.platform.net.dns.service.nameresolver.INameResolver;
import com.netifera.platform.net.http.internal.tools.Activator;
import com.netifera.platform.net.http.web.model.WebSiteEntity;
import com.netifera.platform.tools.RequiredOptionMissingException;
import com.netifera.platform.util.addresses.inet.IPv4Address;
import com.netifera.platform.util.addresses.inet.IPv6Address;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.locators.TCPSocketLocator;
import com.netifera.platform.util.patternmatching.InternetAddressMatcher;

public class AddWebSite  implements ITool {
	
	private String url;
	private INameResolver resolver;
	
	private IToolContext context;
	private long realm;

	public void toolRun(IToolContext context) throws ToolException {
		this.context = context;

		// XXX hardcode local probe as realm
		IProbe probe = Activator.getInstance().getProbeManager().getLocalProbe();
		realm = probe.getEntity().getId();

		context.setTitle("Add web site");
		
		setupToolOptions();

		try {
			URI actualURL = new URI(url);
			context.setTitle("Add web site "+actualURL);
			int port = actualURL.getPort() == -1 ? 80 : actualURL.getPort();
			String hostname = actualURL.getHost();
			List<InternetAddress> addresses;
			if (InternetAddressMatcher.matches(hostname)) {
				addresses = new ArrayList<InternetAddress>(1);
				addresses.add(InternetAddress.fromString(hostname));
			} else {
				try {
					addresses = resolver.getAddressesByName(hostname);
				} catch (UnknownHostException e) {
					context.error("Unknown host: "+actualURL.getHost());
					return;
				} catch (TextParseException e) {
					context.error("Unable to parse host: "+actualURL.getHost());
					return;
				}
				for (InternetAddress address : addresses) {
					context.info(hostname+" has address "+address);
					if (address instanceof IPv6Address) {
						Activator.getInstance().getDomainEntityFactory().createAAAARecord(realm, context.getSpaceId(), hostname, (IPv6Address)address);
					} else {
						Activator.getInstance().getDomainEntityFactory().createARecord(realm, context.getSpaceId(), hostname, (IPv4Address)address);
					}
				}
			}
			
			for (InternetAddress address : addresses) {
				WebSiteEntity entity = Activator.getInstance().getWebEntityFactory().createWebSite(realm, context.getSpaceId(), new TCPSocketLocator(address, port), hostname);
				entity.addTag("Target");
				entity.update();
			}

			// TODO: add credentials if any
		} catch (URISyntaxException e) {
			context.error("Malformed URL: " + url);
		} finally {
			context.done();
		}
	}
	
	private void setupToolOptions() throws RequiredOptionMissingException {
		resolver = Activator.getInstance().getNameResolver();
		url = (String) context.getConfiguration().get("url");
		if (url == null) {
			throw new RequiredOptionMissingException("url");
		}
	}
}
