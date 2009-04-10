package com.netifera.platform.net.daemon.sniffing.modules;

import com.netifera.platform.net.daemon.sniffing.IIPSniffer;
import com.netifera.platform.net.daemon.sniffing.IPacketModuleContext;
import com.netifera.platform.net.packets.tcpip.IPv4;
import com.netifera.platform.net.packets.tcpip.IPv6;
import com.netifera.platform.net.sniffing.IPacketFilter;

public class TestSniffer implements IIPSniffer {

	public String getName() {
		return "Test Sniffer";
	}

	public IPacketFilter getFilter() {
		return null;
	}

	public void handleIPv4Packet(IPv4 ipv4, IPacketModuleContext ctx) {
		ctx.printOutput("IPv4: " + ipv4.print());
		
	}

	public void handleIPv6Packet(IPv6 ipv6, IPacketModuleContext ctx) {
		ctx.printOutput("IPv6: " + ipv6.print());
		
	}
}
