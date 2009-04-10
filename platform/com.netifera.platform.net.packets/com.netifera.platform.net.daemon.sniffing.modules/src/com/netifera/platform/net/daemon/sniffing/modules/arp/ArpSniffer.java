package com.netifera.platform.net.daemon.sniffing.modules.arp;

import com.netifera.platform.net.daemon.sniffing.IArpSniffer;
import com.netifera.platform.net.daemon.sniffing.IPacketModuleContext;
import com.netifera.platform.net.internal.daemon.sniffing.modules.Activator;
import com.netifera.platform.net.model.INetworkEntityFactory;
import com.netifera.platform.net.packets.link.ARP;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.util.addresses.IHardwareAddress;
import com.netifera.platform.util.addresses.INetworkAddress;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class ArpSniffer implements IArpSniffer {

	public IPacketFilter getFilter() {
		return null;
	}

	public String getName() {
		return "ARP Information Gathering";
	}

	
	public void handleArpPacket(ARP arp, IPacketModuleContext ctx) {
		if(arp.isRequest()) {
			handleRequest(arp, ctx.getRealm(), ctx.getSpaceId());
			
		} else if(arp.isReply()) {
			handleReply(arp, ctx.getRealm(), ctx.getSpaceId());
		}	
		
	}
	
	private void handleRequest(ARP request, long realm, long view) {
		INetworkAddress address = request.getSenderProtocolAddress();
		IHardwareAddress mac = request.getSenderHardwareAddress();
		if(address instanceof InternetAddress) {
			handleInternetAddress((InternetAddress)address, mac, realm, view);
		}
	}
	
	private void handleReply(ARP reply, long realm, long view) {
		INetworkAddress address = reply.getSenderProtocolAddress();
		IHardwareAddress mac = reply.getSenderHardwareAddress();
		if(address instanceof InternetAddress) {
			handleInternetAddress((InternetAddress)address, mac, realm, view);
		}
		address = reply.getTargetProtocolAddress();
		mac = reply.getTargetHardwareAddress();
		if(address instanceof InternetAddress) {
			handleInternetAddress((InternetAddress)address, mac, realm, view);
		}
	}

	private void handleInternetAddress(InternetAddress addr, IHardwareAddress macAddr, long realm, long view) {
		if (addr.isUnspecified()) {
			return;
		}
		// FIXME add multicast?
		
		INetworkEntityFactory factory = Activator.getInstance().getNetworkEntityFactory();
		factory.createAddress(realm, view, addr);
		
		// TODO add MAC to model (InternetAddressEntity.setMacAddress()?)
	}
}
