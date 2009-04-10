package com.netifera.platform.net.internal.pcap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.osgi.service.component.ComponentContext;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.system.ISystemService;
import com.netifera.platform.net.internal.pcap.linux.LinuxPacketCapture;
import com.netifera.platform.net.internal.pcap.osx.OsxPacketCapture;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.pcap.IPacketCapture;
import com.netifera.platform.net.pcap.IPacketCaptureFactoryService;
import com.netifera.platform.net.pcap.IPacketHandler;

public class PacketCaptureFactoryService implements
		IPacketCaptureFactoryService {

	private ISystemService system;
	private ILogger logger;

	private final Set<ICaptureInterface> interfaces = new HashSet<ICaptureInterface>();
	
	public IPacketCapture create(ICaptureInterface iface, int snaplen, boolean promiscuous,
			int timeout, IPacketHandler packetHandler) {
		
	
		
		PacketCapture pcap = new PacketCapture(iface.getName(), snaplen, promiscuous, timeout, packetHandler);
		
		INativePacketCapture nativePcap = createNative(pcap);
		pcap.setNativeCapture(nativePcap);
		
		return pcap;
	}
	
	
	public Collection<ICaptureInterface> getInterfaces() {
		synchronized(interfaces) {
			return new HashSet<ICaptureInterface>(interfaces);
		}
	}
	
	public Collection<ICaptureInterface> getCurrentInterfaces() {
		try {
			initializeInterfaces();
		} catch (SocketException e) {
			return Collections.emptySet();
		}
		return getInterfaces();
	}
	
	private INativePacketCapture createNative(IPacketCaptureInternal pcap) {
		switch(system.getOS()) {
		case OS_OSX:
			return new OsxPacketCapture(system, pcap, logger);
			
		case OS_LINUX:
			return new LinuxPacketCapture(system, pcap, logger);
			
		default:
			throw new IllegalStateException("No native pcap implementation for current OS");
		}
	
	}

	protected void setSystemService(ISystemService system) {
		this.system = system;
	}
	
	protected void unsetSystemService(ISystemService system) {
		this.system = null;
	}
	
	protected void activate(ComponentContext ctx) {
		try {
			initializeInterfaces();
		} catch (SocketException e) {
			throw new RuntimeException("SocketException initializing interfaces", e);
		}
	}
	
	protected void deactivate(ComponentContext ctx) {
		interfaces.clear();
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("PacketCapture");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		logger = null;
	}
	
	private void initializeInterfaces() throws SocketException {
		
		Enumeration<NetworkInterface> networkInterfaces = null;
		
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch(Exception e) {
			networkInterfaces = null;
		}
		
		if(networkInterfaces == null) {
			initializeFromProc();
			return;
		}
		
		synchronized(interfaces) {
			interfaces.clear();
		
			while(networkInterfaces.hasMoreElements()) {
				NetworkInterface iface = networkInterfaces.nextElement();
				PacketCapture pcap = new PacketCapture(iface.getDisplayName());
				INativePacketCapture nativePcap = createNative(pcap);
				pcap.setNativeCapture(nativePcap);
				interfaces.add(new PhysicalInterface(this, iface, pcap.testOpen()));
			}
		}
			
		
	}
	
	/*
	 * Kind of a hack to make the probe work on Linux
	 */
	
	private boolean initializeFromProc()  {
		File f = new File("/peludo/osfs/proc/net/dev");
		if(!(f.exists() && f.canRead())) {
			return false;
		}
		
		try {
			return readDevicesFromProc(f);
		} catch(IOException e) {
			return false;
		}
		
	}
	
	private boolean readDevicesFromProc(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		if(in.readLine() == null || in.readLine() == null) {
			in.close();
			return false;
		}

		while(true) {
			final String line = in.readLine();
			if(line == null)
				break;

			final String deviceName = line.split(":")[0].trim();

			interfaces.add(new PseudoInterface(this, deviceName));

		}
		in.close();
		return true;
	}
}
