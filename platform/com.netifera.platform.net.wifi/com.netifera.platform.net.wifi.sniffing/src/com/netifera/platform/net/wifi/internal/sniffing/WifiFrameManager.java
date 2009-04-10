package com.netifera.platform.net.wifi.internal.sniffing;

import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.IPacketContext;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.ISnifferHandle;
import com.netifera.platform.net.sniffing.util.AbstractPacketManager;
import com.netifera.platform.net.sniffing.util.ISniffingEngineEx;
import com.netifera.platform.net.wifi.packets.WiFiFrame;

public class WifiFrameManager extends AbstractPacketManager<WiFiFrame>{

	private  ISnifferHandle rawHandle;
	private final IPacketFilter filter = null;
	
	WifiFrameManager(ISniffingEngineEx sniffingEngine, ICaptureInterface captureInterface) {
		super(sniffingEngine, captureInterface);
	}
	
	private void handleRaw(IPacketHeader packet, IPacketContext ctx) {
		WiFiFrame wifi = (WiFiFrame) packet.findHeader(WiFiFrame.class);
		if(wifi != null) {
			deliverPacket(wifi, ctx);
		}	
	}

	@Override
	protected boolean start() {
		rawHandle = getSniffingEngine().createRawHandle(getInterface(), filter, new IPacketSniffer<IPacketHeader>() {

			public void handlePacket(IPacketHeader packet, IPacketContext ctx) {
				handleRaw(packet, ctx);
			}
			
		});
		rawHandle.setHighPriority();
		
		rawHandle.register();
		return true;
	}

	@Override
	protected void stop() {
		rawHandle.unregister();	
		rawHandle = null;
	}

}
