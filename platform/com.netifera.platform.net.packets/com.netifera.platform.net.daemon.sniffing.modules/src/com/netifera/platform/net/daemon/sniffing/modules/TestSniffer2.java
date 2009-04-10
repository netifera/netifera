package com.netifera.platform.net.daemon.sniffing.modules;

import com.netifera.platform.net.daemon.sniffing.IArpSniffer;
import com.netifera.platform.net.daemon.sniffing.IPacketModuleContext;
import com.netifera.platform.net.packets.link.ARP;
import com.netifera.platform.net.sniffing.IPacketFilter;

public class TestSniffer2 implements IArpSniffer {

	public IPacketFilter getFilter() {
		return null;
	}

	public String getName() {
		return "Test Sniffer 2";
	}

	public void handleArpPacket(ARP arp, IPacketModuleContext ctx) {
		ctx.printOutput("ARP: " + arp.print());		
	}

}
