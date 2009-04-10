package com.netifera.platform.net.wifi.tools;

import com.netifera.platform.net.daemon.sniffing.IPacketModuleContext;
import com.netifera.platform.net.packets.tcpip.IP;
import com.netifera.platform.net.wifi.model.ExtendedServiceSetEntity;
import com.netifera.platform.net.wifi.model.WirelessStationEntity;
import com.netifera.platform.net.wifi.packets.DataFrame;

public class DataFrameProcessor {
	
	private final WirelessScanner scanner;
	
	DataFrameProcessor(WirelessScanner scanner) {
		this.scanner = scanner;
	}
	
	void processFrame(DataFrame frame, IPacketModuleContext ctx) {
		final boolean toDS = frame.isToDistribution();
		final boolean fromDS = frame.isFromDistribution();
		
		
		ExtendedServiceSetEntity ess = findESS(frame);
		if(ess == null) {
			ctx.abortProcessing();
			return;
		} else {
			
			System.out.println("[" + ess.getName() + "] " + frame.print());
			ctx.setRealm(ess.getId());
		}
		
		WirelessStationEntity station;
		if(toDS && !fromDS) {
			station = scanner.discoverStation(frame.source(), frame.destination(), ctx);
			IP ip = (IP) frame.findHeader(IP.class);
			if(station == null || ip == null) return;
			scanner.discoverIP(ip.getSourceAddress(), station, ctx);
			
		} else if(fromDS && !toDS) {
			station = scanner.discoverStation(frame.destination(), frame.source(), ctx);
			IP ip = (IP) frame.findHeader(IP.class);
			if(station == null || ip == null) return;
			scanner.discoverIP(ip.getDestinationAddress(), station, ctx);
		}
		
			
		
		
	}
	
	
	private ExtendedServiceSetEntity findESS(DataFrame frame) {
		if(frame.isFromDistribution()) {
			return scanner.lookupESS(frame.source());
		} else if(frame.isToDistribution()) {
			return scanner.lookupESS(frame.destination());
		} else {
			return null;
		}
	}

}
