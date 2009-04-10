package com.netifera.platform.net.internal.sniffing.managers;

import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.sniffing.IPacketContext;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.util.AbstractPacketManager;
import com.netifera.platform.net.sniffing.util.CaptureFileInterface;
import com.netifera.platform.net.sniffing.util.IPacketSource;
import com.netifera.platform.net.sniffing.util.ISniffingEngineEx;

public class CaptureFileManager extends AbstractPacketManager<IPacketHeader> implements IPacketSource {

	public CaptureFileManager(ISniffingEngineEx engine,
			CaptureFileInterface iface) {
		super(engine, iface);
		
		iface.setSniffer(new IPacketSniffer<IPacketHeader>() {

			public void handlePacket(IPacketHeader packet, IPacketContext ctx) {
				deliverPacket(packet, ctx);				
			}
		});
	}

	@Override
	protected boolean start() {
		return true;
	}

	@Override
	protected void stop() {		
	}

	public void setRealm(long realmId) {
		// TODO Auto-generated method stub
		
	}

}
