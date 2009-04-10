package com.netifera.platform.net.dns.service;

import java.io.IOException;
import java.util.List;

import org.xbill.DNS.Name;
import org.xbill.DNS.ZoneTransferException;
import org.xbill.DNS.ZoneTransferIn;

import com.netifera.platform.net.dns.service.client.ExtendedResolver;
import com.netifera.platform.net.dns.service.client.SimpleResolver;
import com.netifera.platform.net.dns.service.nameresolver.INameResolver;
import com.netifera.platform.net.dns.service.nameresolver.NameResolver;
import com.netifera.platform.net.services.NetworkService;
import com.netifera.platform.net.sockets.ISocketEngineService;
import com.netifera.platform.net.sockets.UDPChannel;
import com.netifera.platform.util.locators.ISocketLocator;
import com.netifera.platform.util.locators.UDPSocketLocator;

public class DNS extends NetworkService {
	private static final long serialVersionUID = 964019196230856576L;

	public DNS(ISocketLocator locator) {
		super(locator);
	}
	
	@Override
	public UDPSocketLocator getLocator() {
		return (UDPSocketLocator) super.getLocator();
	}

	private SimpleResolver createSimpleResolver(ISocketEngineService engine) throws IOException {
		UDPChannel channel = engine.openUDP();
		channel.connect(getLocator());
		return new SimpleResolver(channel);
	}
	
	private ExtendedResolver createExtendedResolver(ISocketEngineService engine) throws IOException {
		ExtendedResolver answer = new ExtendedResolver();
		answer.addResolver(createSimpleResolver(engine));
		return answer;
	}
	
	public INameResolver createNameResolver(ISocketEngineService engine) throws IOException {
		return new NameResolver(createExtendedResolver(engine));
	}

	public List<?> zoneTransfer(String domain) throws IOException, ZoneTransferException {
		ZoneTransferIn xfr = ZoneTransferIn.newAXFR(new Name(domain), getLocator().getAddress().toInetAddress().getHostAddress(), getLocator().getPort(), null);
		return xfr.run();
	}
	
	public int getDefaultPort() {
		return 53;
	}
	
	// TODO see RFC 4501 for getURI()
}
