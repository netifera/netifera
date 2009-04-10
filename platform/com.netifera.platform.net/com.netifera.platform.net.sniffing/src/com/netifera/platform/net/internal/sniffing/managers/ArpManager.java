package com.netifera.platform.net.internal.sniffing.managers;

import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.link.ARP;
import com.netifera.platform.net.sniffing.IPacketContext;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.IPacketSnifferHandle;
import com.netifera.platform.net.sniffing.util.AbstractPacketManager;

public class ArpManager extends AbstractPacketManager<ARP>{
	private final IPacketSnifferHandle<IPacketHeader> packetHandle;
	private final IPacketFilter filter = null;
	public ArpManager(IPacketManager<IPacketHeader> packetManager) {
		super(packetManager.getSniffingEngine(), packetManager.getInterface());
		
		packetHandle = new SnifferHandle<IPacketHeader>(packetManager, filter,
				 new IPacketSniffer<IPacketHeader>() {
			
					public void handlePacket(IPacketHeader packet, IPacketContext ctx) {
						handleRaw(packet, ctx);						
					}	
		});
	}

	private void handleRaw(IPacketHeader packet, IPacketContext ctx) {
		ARP arp = (ARP) packet.findHeader(ARP.class);
		if(arp != null) {
			deliverPacket(arp, ctx);
		}		
	}
	
	@Override
	protected boolean start() {
		packetHandle.register();
		return true;
	}

	@Override
	protected void stop() {
		packetHandle.unregister();		
	}

}
