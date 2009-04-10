package com.netifera.platform.net.daemon.sniffing.modules.detection;

import java.nio.ByteBuffer;
import java.util.Map;

import com.netifera.platform.net.daemon.sniffing.IIPSniffer;
import com.netifera.platform.net.daemon.sniffing.IPacketModuleContext;
import com.netifera.platform.net.daemon.sniffing.IStreamModuleContext;
import com.netifera.platform.net.daemon.sniffing.ITCPBlockSniffer;
import com.netifera.platform.net.internal.daemon.sniffing.modules.Activator;
import com.netifera.platform.net.model.INetworkEntityFactory;
import com.netifera.platform.net.packets.tcpip.IP;
import com.netifera.platform.net.packets.tcpip.IPv4;
import com.netifera.platform.net.packets.tcpip.IPv6;
import com.netifera.platform.net.packets.tcpip.UDP;
import com.netifera.platform.net.services.credentials.Credential;
import com.netifera.platform.net.services.credentials.Password;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;
import com.netifera.platform.net.services.detection.IClientDetectorService;
import com.netifera.platform.net.services.detection.IServerDetectorService;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.stream.IBlockSnifferConfig;
import com.netifera.platform.net.sniffing.stream.ISessionKey;
import com.netifera.platform.util.locators.ISocketLocator;
import com.netifera.platform.util.locators.TCPSocketLocator;
import com.netifera.platform.util.locators.UDPSocketLocator;

public class PassiveServiceDetector implements ITCPBlockSniffer, IIPSniffer {
	
	public IPacketFilter getFilter() {
		return null;
	}

	public String getName() {
		return "Passive Service Detector";
	}

	public void initialize(IBlockSnifferConfig config) {
		config.setTotalLimit(1024);
	}

	public void handleBlock(IStreamModuleContext ctx, ByteBuffer clientData, ByteBuffer serverData) {
		handleTCPBlock(ctx, clientData, serverData);
	}
	
	private void handleTCPBlock(IStreamModuleContext ctx, ByteBuffer clientData, ByteBuffer serverData) {
		final ISessionKey key = ctx.getKey();
		final long realm = ctx.getRealm();
		final long space = ctx.getSpaceId();
		Map<String,String> clientInfo, serverInfo;
		TCPSocketLocator locator = new TCPSocketLocator(key.getServerAddress(), key.getServerPort());
		
		clientInfo = Activator.getInstance().getClientDetector().detect(
				"tcp", key.getServerPort(), clientData, serverData);

		clientData.rewind();
		serverData.rewind();
		
		serverInfo = Activator.getInstance().getServerDetector().detect(
					"tcp", key.getServerPort(), clientData, serverData);

		String serviceType = null;
		if(serverInfo != null) {
			serviceType = serverInfo.get("serviceType");
		} else if (clientInfo != null) {
			serviceType = clientInfo.get("serviceType");
		}

		if (serviceType != null) {
			Activator.getInstance().getNetworkEntityFactory().createService(realm, space, locator, serviceType, serverInfo);
			Activator.getInstance().getNetworkEntityFactory().createClient(realm, space, key.getClientAddress(), serviceType, clientInfo, locator);
			
			clientData.rewind();
			serverData.rewind();

			sniffCredentials(locator, serviceType, clientData, serverData, realm, space);
		}
	}

	public void handleIPv4Packet(IPv4 ipv4, IPacketModuleContext context) {
		handleIPPacket(ipv4, context);
	}
	
	public void handleIPv6Packet(IPv6 ipv6, IPacketModuleContext context) {
		handleIPPacket(ipv6, context);
	}
	
	private void handleIPPacket(IP ip, IPacketModuleContext ctx) {
		final long realm = ctx.getRealm();
		final long view = ctx.getSpaceId();
		final IClientDetectorService clientDetector = Activator.getInstance().getClientDetector();
		final IServerDetectorService serverDetector = Activator.getInstance().getServerDetector();
		final INetworkEntityFactory factory = Activator.getInstance().getNetworkEntityFactory();
		if(clientDetector == null || serverDetector == null || factory == null)
			return;
		
		if (ip.getNextHeader() instanceof UDP) {
			UDP udp = (UDP) ip.getNextHeader();
			ByteBuffer empty = ByteBuffer.allocate(0);
			Map<String,String> clientInfo, serverInfo;
			if(udp.payload() == null) return;
			clientInfo = clientDetector.detect("udp", udp.getDestinationPort(), udp.payload().toByteBuffer(), empty);
			if (clientInfo != null) {
				UDPSocketLocator locator = new UDPSocketLocator(ip.getDestinationAddress(), udp.getDestinationPort());
				String serviceType = clientInfo.get("serviceType");
				factory.createService(realm, view, locator, serviceType, null);
				factory.createClient(realm, view, ip.getSourceAddress(), serviceType, clientInfo, locator);
				sniffCredentials(locator, serviceType, udp.payload().toByteBuffer(), empty, realm, view);
			} else {
				serverInfo = serverDetector.detect("udp", udp.getSourcePort(), empty, udp.payload().toByteBuffer());
				if (serverInfo != null) {
					UDPSocketLocator locator = new UDPSocketLocator(ip.getSourceAddress(), udp.getSourcePort());
					String serviceType = serverInfo.get("serviceType");
					factory.createService(realm, view, locator, serviceType, serverInfo);
					factory.createClient(realm, view, ip.getDestinationAddress(), serviceType, null, locator);
					sniffCredentials(locator, serviceType, empty, udp.payload().toByteBuffer(), realm, view);
				}
			}
		}
	}
	
	private void sniffCredentials(ISocketLocator locator, String serviceType, ByteBuffer clientData, ByteBuffer serverData, long realm, long view) {
		Credential credential = Activator.getInstance().getCredentialSniffer().sniff(serviceType, clientData, serverData);
		if(credential != null) {
			if (credential instanceof UsernameAndPassword) {
				UsernameAndPassword c = (UsernameAndPassword) credential;
				Activator.getInstance().getNetworkEntityFactory().createUsernameAndPassword(realm, view, locator, c.getUsernameString(), c.getPasswordString());
			} else if (credential instanceof Password) {
				Password c = (Password) credential;
				Activator.getInstance().getNetworkEntityFactory().createPassword(realm, view, locator, c.getPasswordString());
			}
		}
	}
}
