package com.netifera.platform.net.wifi.internal.pcap;

import java.util.ArrayList;
import java.util.Collection;

import org.osgi.service.component.ComponentContext;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.system.ISystemService;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.pcap.IPacketCapture;
import com.netifera.platform.net.pcap.IPacketCaptureFactoryService;
import com.netifera.platform.net.pcap.IPacketHandler;
import com.netifera.platform.net.wifi.internal.pcap.linux.LinuxWifiCapture;
import com.netifera.platform.net.wifi.internal.pcap.linux.LinuxWifiInterfaceTester;
import com.netifera.platform.net.wifi.internal.pcap.osx.OsxWifiCapture;
import com.netifera.platform.net.wifi.internal.pcap.osx.OsxWifiInterfaceTester;
import com.netifera.platform.net.wifi.pcap.IWifiPacketCapture;
import com.netifera.platform.net.wifi.pcap.IWifiPacketCaptureFactory;
import com.netifera.platform.net.wifi.pcap.IWirelessCaptureInterface;

public class WifiPacketCaptureFactory implements IWifiPacketCaptureFactory {

	private ISystemService system;
	private IPacketCaptureFactoryService pcapFactory;
	private Collection<IWirelessCaptureInterface> wifiInterfaces;

	public IWifiPacketCapture create(IWirelessCaptureInterface iface, int snaplen, boolean promiscuous,
			int timeout, IPacketHandler packetHandler) {
		IPacketCapture pcap = pcapFactory.create(iface.getInterface(), snaplen, promiscuous, timeout, packetHandler);
		IWifiNative wifiNative = createNative(pcap);
		return new WifiPacketCapture(pcap, wifiNative);
	}

	public Collection<IWirelessCaptureInterface> getWifiInterfaces() {
		return wifiInterfaces;
	}

	private IWifiNative createNative(IPacketCapture pcap) {
		switch(system.getOS()) {
		case OS_LINUX:
			return new LinuxWifiCapture(system, pcap);
		case OS_OSX:
			return new OsxWifiCapture(system, pcap);
		default:
			throw new IllegalStateException("No native wifi pcap implementation for current os");
		}
	}
	
	private IWifiInterfaceTester createInterfaceTester() {
		switch(system.getOS()) {
		case OS_OSX:
			return new OsxWifiInterfaceTester(pcapFactory);
		case OS_LINUX:
			return new LinuxWifiInterfaceTester();
		default:
			throw new IllegalStateException("No native wifi pcap implementation for current os");
		}
	}
	
	protected void activate(ComponentContext ctx) {
		initializeInterfaces();
	}
	
	protected void setSystemService(ISystemService system) {
		this.system = system;
	}
	
	protected void unsetSystemService(ISystemService system) {
		this.system = null;
	}
	
	protected void setPcapFactory(IPacketCaptureFactoryService factory) {
		pcapFactory = factory;	
	}
	
	protected void unsetPcapFactory(IPacketCaptureFactoryService factory) {
		
	}
	private void initializeInterfaces() {
		IWifiInterfaceTester tester = createInterfaceTester();
		wifiInterfaces = new ArrayList<IWirelessCaptureInterface>();
		
		for(ICaptureInterface iface : pcapFactory.getInterfaces()) {
			if(tester.isWifiDevice(iface)) {
				wifiInterfaces.add(new WirelessCaptureInterface(this, iface));
			} 
		}
	}
	
	/* logging */

	private ILogger logger;
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("PacketCapture [WiFi]");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		logger = null;
	}

	public ILogger getLogger() {
		return logger;
	}
}
