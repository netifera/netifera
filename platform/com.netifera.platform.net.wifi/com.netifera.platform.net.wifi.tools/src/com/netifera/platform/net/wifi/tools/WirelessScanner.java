package com.netifera.platform.net.wifi.tools;

import java.util.HashMap;
import java.util.Map;

import com.netifera.platform.net.daemon.sniffing.IPacketModuleContext;
import com.netifera.platform.net.model.INetworkEntityFactory;
import com.netifera.platform.net.model.InternetAddressEntity;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.wifi.model.AccessPointEntity;
import com.netifera.platform.net.wifi.model.ExtendedServiceSetEntity;
import com.netifera.platform.net.wifi.model.IWirelessEntityFactory;
import com.netifera.platform.net.wifi.model.WirelessStationEntity;
import com.netifera.platform.net.wifi.packets.DataFrame;
import com.netifera.platform.net.wifi.packets.ManagementFrame;
import com.netifera.platform.net.wifi.packets.WiFiFrame;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffer;
import com.netifera.platform.util.addresses.MACAddress;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class WirelessScanner implements IWifiSniffer {

	private final DataFrameProcessor dataProcessor;
	private final ManagementFrameProcessor managementProcessor;
	
	private IWirelessEntityFactory wirelessEntityFactory;
	private INetworkEntityFactory networkEntityFactory;
	
	private Map<String, ExtendedServiceSetEntity> essCache =
		new HashMap<String, ExtendedServiceSetEntity>();
	
	private Map<MACAddress, AccessPointEntity> apCache = 
		new HashMap<MACAddress, AccessPointEntity>();
	
	private Map<MACAddress, WirelessStationEntity> stationCache = 
		new HashMap<MACAddress, WirelessStationEntity>();
	
	private Map<MACAddress, ExtendedServiceSetEntity> bssToEss =
		new HashMap<MACAddress, ExtendedServiceSetEntity>();
		
	private Map<InternetAddress, WirelessStationEntity> ipToStation =
		new HashMap<InternetAddress, WirelessStationEntity>();
	
	public WirelessScanner() {
		dataProcessor = new DataFrameProcessor(this);
		managementProcessor = new ManagementFrameProcessor(this);
	}
	
	public void handleWifiFrame(WiFiFrame frame, IPacketModuleContext ctx) {
		if(frame instanceof ManagementFrame) {
			managementProcessor.processFrame((ManagementFrame) frame, ctx);
		}
		if(frame instanceof DataFrame) {
			dataProcessor.processFrame((DataFrame) frame, ctx);
			
		}
		
	}

	void discoverAP(MACAddress bssid, IPacketModuleContext ctx) {
		cacheAP(bssid, ctx);
	}
	
	WirelessStationEntity discoverStation(MACAddress address, MACAddress bssid, IPacketModuleContext ctx) {
		if(address.isBroadcast() || bssid.isBroadcast())
			return null;
		return cacheStation(address, bssid, ctx);
	}
	
	void discoverESS(MACAddress bss, String name, boolean encrypted, IPacketModuleContext ctx) {
		
		final ExtendedServiceSetEntity ess = cacheESS(name, ctx);
		if(encrypted) {
			ess.setEncrypted(true);
			ess.save();
		}
		cacheBSStoESS(bss, ess);
		cacheAP(bss, ctx);
		
	}
	
	void discoverIP(InternetAddress address, WirelessStationEntity station, IPacketModuleContext ctx) {
		if(ipToStation.containsKey(address))
			return;
		InternetAddressEntity addressEntity = networkEntityFactory.createAddress(ctx.getRealm(), ctx.getSpaceId(), address);
		ipToStation.put(address, station);
		station.setNetworkAddress(addressEntity);
		station.save();
	}
	
	private ExtendedServiceSetEntity cacheESS(String name, IPacketModuleContext ctx) {
		if(essCache.containsKey(name)) {
			return essCache.get(name);
		}
		final ExtendedServiceSetEntity ess = wirelessEntityFactory.createExtendedServiceSet(ctx.getRealm(), ctx.getSpaceId(), name);
		essCache.put(name, ess);
		return ess;
		
	}
	
	private AccessPointEntity cacheAP(MACAddress bssid, IPacketModuleContext ctx) {
		if(apCache.containsKey(bssid))
			return apCache.get(bssid);
		final AccessPointEntity ap = wirelessEntityFactory.createAccessPoint(ctx.getRealm(), ctx.getSpaceId(), bssid, lookupESS(bssid));
		apCache.put(bssid, ap);
		return ap;
	}
	
	private WirelessStationEntity cacheStation(MACAddress address, MACAddress bssid, IPacketModuleContext ctx) {
		if(stationCache.containsKey(address))
			return stationCache.get(address);
		
		final WirelessStationEntity station = wirelessEntityFactory.createStation(ctx.getRealm(), ctx.getSpaceId(), address, lookupESS(bssid));
		stationCache.put(address, station);
		return station;
	}
	
	private void cacheBSStoESS(MACAddress bss, ExtendedServiceSetEntity ess) {
		if(bssToEss.containsKey(bss))
			return;
		
		System.out.println("adding " + bss + " --> " + ess.getName());
		bssToEss.put(bss, ess);
	}
	
	public ExtendedServiceSetEntity lookupESS(MACAddress bss) {
		if(!bssToEss.containsKey(bss)) 
			return null;
		return bssToEss.get(bss);
		
	}
	
	
	public IPacketFilter getFilter() {
		return null;
	}

	public String getName() {
		return "Wireless Network Discovery";
	}
	
	protected void setWirelessEntityFactory(IWirelessEntityFactory factory) {
		wirelessEntityFactory = factory;
	}
	
	protected void unsetWirelessEntityFactory(IWirelessEntityFactory factory) {
		
	}
	
	protected void setNetworkEntityFactory(INetworkEntityFactory factory) {
		networkEntityFactory = factory;
	}
	
	protected void unsetNetworkEntityFactory(INetworkEntityFactory factory) {
		
	}

}
