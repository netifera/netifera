package com.netifera.platform.net.internal.sniffing;

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
import com.netifera.platform.net.pcap.IPacketCaptureFactoryService;
import com.netifera.platform.net.pcap.ex.ICaptureInterfaceEx;
import com.netifera.platform.net.sniffing.ICaptureFileInterface;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.IPacketSnifferHandle;
import com.netifera.platform.net.sniffing.stream.IBlockSniffer;
import com.netifera.platform.net.sniffing.stream.IBlockSnifferHandle;
import com.netifera.platform.net.sniffing.stream.IStreamSniffer;
import com.netifera.platform.net.sniffing.stream.IStreamSnifferHandle;
import com.netifera.platform.net.sniffing.util.CaptureFileInterface;
import com.netifera.platform.net.sniffing.util.IBasicInterfaceManager;
import com.netifera.platform.net.sniffing.util.IPacketSource;
import com.netifera.platform.net.sniffing.util.ISniffingEngineEx;
import com.netifera.platform.net.sniffing.util.InterfaceManager;

public class SniffingEngineService implements ISniffingEngineEx {
	private final static int DEFAULT_SNAPLEN = 65535;
	private final static int DEFAULT_TIMEOUT = 500;
	
	private final int snaplen = DEFAULT_SNAPLEN;
	private final int timeout = DEFAULT_TIMEOUT;
	private final boolean promiscuous = true;
	private IPacketCaptureFactoryService pcapFactory;
	private ILogger logger;
	
	private final Map<ICaptureInterface, InterfaceManager> physicalInterfaces = new HashMap<ICaptureInterface, InterfaceManager>();
	
	private final Map<CaptureFileInterface, InterfaceManager> captureFileInterfaces =
		new HashMap<CaptureFileInterface, InterfaceManager>();
	
	public Collection<ICaptureInterface> getInterfaces() {
		return pcapFactory.getCurrentInterfaces();
	}
	
	public ICaptureInterfaceEx getInterfaceByName(String name) {
		for(ICaptureInterface iface : getInterfaces()) {
			if(iface.getName().equals(name) && iface instanceof ICaptureInterfaceEx)
				return (ICaptureInterfaceEx) iface;
		}
		return null;
	}
	
	public IPacketSnifferHandle<IPacketHeader> createRawHandle(ICaptureInterface iface,
			IPacketFilter filter, IPacketSniffer<IPacketHeader> sniffer) {
		return getManagerForInterface(iface).createRawHandle(filter, sniffer);
	}
	
	public IPacketSnifferHandle<ARP> createArpHandle(ICaptureInterface iface,
			IPacketFilter filter, IPacketSniffer<ARP> sniffer) {
		return getManagerForInterface(iface).createArpHandle(filter, sniffer);
	}
	
	public IPacketSnifferHandle<IPv4> createIPv4Handle(ICaptureInterface iface,
			IPacketFilter filter, IPacketSniffer<IPv4> sniffer) {
		return getManagerForInterface(iface).createIPv4Handle(filter, sniffer);
	}
	
	public IPacketSnifferHandle<IPv6> createIPv6Handle(ICaptureInterface iface,
			IPacketFilter filter, IPacketSniffer<IPv6> sniffer) {
		return getManagerForInterface(iface).createIPv6Handle(filter, sniffer);
	}
	
	public IStreamSnifferHandle createTcpStreamHandle(ICaptureInterface iface,
			IPacketFilter filter, IStreamSniffer sniffer) {
		return getManagerForInterface(iface).createTCPStreamHandle(filter, sniffer);
	}
	
	public IBlockSnifferHandle createTcpBlockHandle(ICaptureInterface iface,
			IPacketFilter filter, IBlockSniffer sniffer) {
		return getManagerForInterface(iface).createTCPBlockHandle(filter, sniffer);
		
	}
	
	public ICaptureFileInterface createCaptureFileInterface(String path) {
		final CaptureFileInterface iface = new CaptureFileInterface(path, this);
		
		if(iface.isValid()) {
			// XXX Should use weak hash map?
			captureFileInterfaces.put(iface, InterfaceManager.createCaptureFileManager(this, iface));
		}
		
		return iface;
	}
	
	public void removeCaptureFileInterface(CaptureFileInterface iface) {
		captureFileInterfaces.remove(iface);
	}
	
	private InterfaceManager getManagerForInterface(ICaptureInterface iface) {
		if (iface == null) {
			throw new IllegalArgumentException("capture interface is null");
		}
		if(!iface.captureAvailable()) {
			throw new IllegalArgumentException("Attempt to register handle on unavailable interface: " + iface.getName());
		}
		if (physicalInterfaces.containsKey(iface)) {
			return physicalInterfaces.get(iface);
		} else if(captureFileInterfaces.containsKey(iface)) {
			return captureFileInterfaces.get(iface);
		} else {
			throw new IllegalArgumentException("No interface found for specified handle: " + iface);
		}
		
	}
	
	private void initializeInterfaces() {
		physicalInterfaces.clear();
		
		for(ICaptureInterface iface : pcapFactory.getInterfaces()) {
			if(!iface.captureAvailable() || !(iface instanceof ICaptureInterfaceEx)) 
				continue;
			
			physicalInterfaces.put(iface, InterfaceManager.createRawManager(this, (ICaptureInterfaceEx)iface));
		}
	}
	
	public int getSnaplen() {
		return snaplen;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	public boolean getPromiscuous() {
		return promiscuous;
	}
	
	public ILogger getLogger() {
		return logger;
	}
	
	protected void setPcapFactory(IPacketCaptureFactoryService factory) {
		pcapFactory = factory;
	}
	
	protected void unsetPcapFactory(IPacketCaptureFactoryService factory) {
		pcapFactory = null;
	}
	
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Sniffing Engine");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}
	
	protected void activate(ComponentContext ctx) {
		initializeInterfaces();
	}
	
	protected void deactivate(ComponentContext cxt) {
		physicalInterfaces.clear();
	}

	
	public IBasicInterfaceManager createInterfaceManager(
			IPacketSource packetManager) {
		return InterfaceManager.createBasic(packetManager);
	}
	
}
