package com.netifera.platform.net.wifi.tools;

import com.netifera.platform.net.daemon.sniffing.IPacketModuleContext;
import com.netifera.platform.net.wifi.packets.Beacon;
import com.netifera.platform.net.wifi.packets.ManagementFrame;
import com.netifera.platform.util.addresses.MACAddress;

public class ManagementFrameProcessor {
	
	private final WirelessScanner scanner;
	
	ManagementFrameProcessor(WirelessScanner scanner) {
		this.scanner = scanner;
	}
	
	void processFrame(ManagementFrame frame, IPacketModuleContext ctx) {
		if(frame instanceof Beacon) {
			processBeacon((Beacon) frame, ctx);
			return;
		}
	}
	
	private void processBeacon(Beacon frame, IPacketModuleContext ctx) {
		String ssid = frame.ssid();
		MACAddress bss = frame.source();
		if(ssid != null) 
			scanner.discoverESS(bss, ssid, frame.capabilities().privacy(), ctx);
	}

}
