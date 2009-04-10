package com.netifera.platform.net.internal.sniffing.managers;

import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.tcpip.IPv6;
import com.netifera.platform.net.sniffing.IPacketContext;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.IPacketSnifferHandle;
import com.netifera.platform.net.sniffing.util.AbstractPacketManager;

public class IPv6Manager extends AbstractPacketManager<IPv6> {

	private final IPacketSnifferHandle<IPacketHeader> packetHandle;
	private final IPacketFilter filter = null;
	
	public IPv6Manager(IPacketManager<IPacketHeader> packetManager) {
		super(packetManager.getSniffingEngine(), packetManager.getInterface());
		
		packetHandle = new SnifferHandle<IPacketHeader>(packetManager, filter,
				new IPacketSniffer<IPacketHeader>() {

					public void handlePacket(IPacketHeader packet,
							IPacketContext ctx) {
						handleRaw(packet, ctx);
					}

		});
	}
	
	private void handleRaw(IPacketHeader packet, IPacketContext ctx) {
		IPv6 ip = (IPv6) packet.findHeader(IPv6.class);
		if(ip != null) {
			deliverPacket(ip, ctx);
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
