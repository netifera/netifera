package com.netifera.platform.net.dns.internal.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.StringTokenizer;

import org.osgi.service.component.ComponentContext;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.dns.service.client.ExtendedResolver;
import com.netifera.platform.net.dns.service.client.SimpleResolver;
import com.netifera.platform.net.dns.service.nameresolver.NameResolver;
import com.netifera.platform.net.sockets.ISocketEngineService;
import com.netifera.platform.net.sockets.UDPChannel;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.locators.UDPSocketLocator;

public class NameResolverService extends NameResolver {
	
	private ISocketEngineService socketEngine;
	private ILogger logger;
	
	protected void setSocketEngine(ISocketEngineService socketEngine) {
		this.socketEngine = socketEngine;
	}
	
	protected void unsetSocketEngine(ISocketEngineService socketEngine) {
		this.socketEngine = null;
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Name Resolver");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		logger = null;
	}
	
	private void addNameServer(String nameServer) throws IOException {
		InternetAddress address = InternetAddress.fromString(nameServer);
		try {
			UDPChannel channel = socketEngine.openUDP();
			channel.connect(new UDPSocketLocator(address, 53));
			SimpleResolver simpleResolver = new SimpleResolver(channel);
			simpleResolver.setLogger(logger);
			resolver.addResolver(simpleResolver);
			logger.debug("added nameserver " + nameServer);
		} catch (SocketException e) { // Network is unreachable
			logger.error("could not add nameserver " + nameServer, e);
		}
	}
	
	private boolean activateUnix() {
		boolean activated = false;
		InputStream in = null;
		try {
			try {
				in = new FileInputStream("/peludo/osfs/etc/resolv.conf");
			} catch (IOException e) {
				in = new FileInputStream("/etc/resolv.conf");
			}
			try {
				InputStreamReader isr = new InputStreamReader(in);
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					if (line.startsWith("nameserver")) {
						StringTokenizer st = new StringTokenizer(line);
						st.nextToken(); /* skip "nameserver" string */
						String nameserver = st.nextToken();
						addNameServer(nameserver);
						logger.info("nameserver "+nameserver);
						activated = true;
					}
				}
			} catch (IOException e) {
				logger.error("I/O error parsing resolv.conf", e);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					logger.error("I/O error closing resolv.conf", e);
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("File /etc/resolv.conf not found. Cannot initialize name resolver service.");
		}
		return activated;
	}
	
	protected void activate(ComponentContext context) {
		resolver = new ExtendedResolver();
		
		//FIXME Unix only
		activateUnix();
		
		if (resolver.getResolvers().length == 0) {
			logger.warning("Could not find any system nameserver, try to add a default one");
			try {
				addNameServer("208.67.222.222"); // resolver1.opendns.com
			} catch (IOException e) {
				logger.error("Could not add default nameserver. Cannot initialize name resolver service.");
			} 
		}
		assert resolver.getResolvers().length != 0 : "NameResolverService: no resolvers found. (imminent ArrayIndexOutOfBoundsException in ExtendedResolver)";
	}
	
	protected void deactivate(ComponentContext context) {
		try {
			shutdown();
		} catch (IOException e) {
			logger.error("I/O error shutdowing resolver service", e);
		}
	}
}
