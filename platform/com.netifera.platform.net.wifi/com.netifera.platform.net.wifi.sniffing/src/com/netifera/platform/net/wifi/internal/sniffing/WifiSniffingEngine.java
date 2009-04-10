package com.netifera.platform.net.wifi.internal.sniffing;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.ComponentContext;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.link.ARP;
import com.netifera.platform.net.packets.tcpip.IPv4;
import com.netifera.platform.net.packets.tcpip.IPv6;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.ICaptureFileInterface;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.ISnifferHandle;
import com.netifera.platform.net.sniffing.ISniffingEngineService;
import com.netifera.platform.net.sniffing.stream.IBlockSniffer;
import com.netifera.platform.net.sniffing.stream.IBlockSnifferHandle;
import com.netifera.platform.net.sniffing.stream.IStreamSniffer;
import com.netifera.platform.net.sniffing.stream.IStreamSnifferHandle;
import com.netifera.platform.net.sniffing.util.CaptureFileInterface;
import com.netifera.platform.net.sniffing.util.IBasicInterfaceManager;
import com.netifera.platform.net.sniffing.util.IPacketSource;
import com.netifera.platform.net.sniffing.util.ISniffingEngineEx;
import com.netifera.platform.net.wifi.packets.WiFiFrame;
import com.netifera.platform.net.wifi.pcap.IWifiPacketCaptureFactory;
import com.netifera.platform.net.wifi.pcap.IWirelessCaptureInterface;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffingEngine;

public class WifiSniffingEngine implements IWifiSniffingEngine {

	
	private IWifiPacketCaptureFactory wifiFactory;
	private ISniffingEngineEx sniffingEngine;
	
	private final Map<IWirelessCaptureInterface, WifiInterfaceManager> interfaces =
		new HashMap<IWirelessCaptureInterface, WifiInterfaceManager>();
	
	
	public Collection<IWirelessCaptureInterface> getWifiInterfaces() {
		return interfaces.keySet();
	}
	
	public ISnifferHandle createWifiHandle(IWirelessCaptureInterface iface,
			IPacketFilter filter, IPacketSniffer<WiFiFrame> sniffer) {
		return getManagerForInterface(iface).createWifiHandle(filter, sniffer);
	}
	
	
	private WifiInterfaceManager getManagerForInterface(ICaptureInterface iface) {
		if (iface == null) {
			throw new IllegalArgumentException("capture interface is null");
		}
		
		if (!interfaces.containsKey(iface)) {
			throw new IllegalArgumentException(
					"No interface found for specified handle : " + iface);
		}
		
		return interfaces.get(iface);
	}
	
	public void removeCaptureFileInterface(CaptureFileInterface iface) {
		// TODO Auto-generated method stub
		
	}

	
	protected void setWifiCaptureFactory(IWifiPacketCaptureFactory factory) {
		wifiFactory = factory;
	}
	
	protected void unsetWifiCaptureFactory(IWifiPacketCaptureFactory factory) {
		wifiFactory = null;
	}
	
	protected void activate(ComponentContext ctx) {
		initializeInterfaces();
	}
	
	protected void deactivate(ComponentContext ctx) {
		disposeInterfaces();
	}
	
	private void initializeInterfaces() {
		for(IWirelessCaptureInterface iface: wifiFactory.getWifiInterfaces()) {
			final IBasicInterfaceManager manager = sniffingEngine.createInterfaceManager(new WirelessRawManager(this, iface));
			interfaces.put(iface, new WifiInterfaceManager(manager, this, iface));
		}
	}
	
	private void disposeInterfaces() {
		for(ICaptureInterface iface : interfaces.keySet()) {
			interfaces.get(iface).dispose();
		}
		interfaces.clear();
	}

	/* logging */

	private ILogger logger;
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("WiFi Sniffing Engine");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		logger = null;
	}

	protected void setSniffingEngine(ISniffingEngineEx sniffingEngine) {
		this.sniffingEngine = sniffingEngine;
	}
	
	protected void unsetSniffingEngine(ISniffingEngineService sniffingEngine) {
		this.sniffingEngine = null;
	}
	public ILogger getLogger() {
		return logger;
	}

	public ISnifferHandle createArpHandle(ICaptureInterface iface,
			IPacketFilter filter, IPacketSniffer<ARP> sniffer) {
		return getManagerForInterface(iface).createArpHandle(filter, sniffer);
	}

	public ICaptureFileInterface createCaptureFileInterface(String path) {
		return sniffingEngine.createCaptureFileInterface(path);
	}

	public ISnifferHandle createIPv4Handle(ICaptureInterface iface,
			IPacketFilter filter, IPacketSniffer<IPv4> sniffer) {
		return getManagerForInterface(iface).createIPv4Handle(filter, sniffer);
	}


	public ISnifferHandle createIPv6Handle(ICaptureInterface iface,
			IPacketFilter filter, IPacketSniffer<IPv6> sniffer) {
		return getManagerForInterface(iface).createIPv6Handle(filter, sniffer);
	}


	public ISnifferHandle createRawHandle(ICaptureInterface iface,
			IPacketFilter filter, IPacketSniffer<IPacketHeader> sniffer) {
		return getManagerForInterface(iface).createRawHandle(filter, sniffer);
	}

	public IBlockSnifferHandle createTcpBlockHandle(ICaptureInterface iface,
			IPacketFilter filter, IBlockSniffer sniffer) {
		return getManagerForInterface(iface).createTCPBlockHandle(filter, sniffer);
	}

	public IStreamSnifferHandle createTcpStreamHandle(ICaptureInterface iface,
			IPacketFilter filter, IStreamSniffer sniffer) {
		return getManagerForInterface(iface).createTCPStreamHandle(filter, sniffer);
	}

	public Collection<ICaptureInterface> getInterfaces() {
		return sniffingEngine.getInterfaces();
	}

	public boolean getPromiscuous() {
		return sniffingEngine.getPromiscuous();
	}

	public int getSnaplen() {
		return sniffingEngine.getSnaplen();
	}

	public int getTimeout() {
		return sniffingEngine.getTimeout();
	}

	public IBasicInterfaceManager createInterfaceManager(
			IPacketSource packetManager) {
		return sniffingEngine.createInterfaceManager(packetManager);
	}
}
