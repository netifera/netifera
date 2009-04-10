package com.netifera.platform.net.wifi.internal.sniffing.daemon;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.sniffing.IPacketContext;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.ISnifferHandle;
import com.netifera.platform.net.wifi.packets.WiFiFrame;
import com.netifera.platform.net.wifi.pcap.IWirelessCaptureInterface;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffer;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffingEngine;

public class EnabledWifiModule {
	private final IWifiSniffingEngine sniffingEngine;
	
	private final IWifiSniffer moduleInstance;
	private final Set<ISnifferHandle> activeHandles = new HashSet<ISnifferHandle>();
	private final ILogger logger;
	
	private boolean running;
	
	public EnabledWifiModule(IWifiSniffingEngine engine, IWifiSniffer module, ILogger logger) {
		this.sniffingEngine = engine;
		this.moduleInstance = module;
		this.logger = logger;
	}

	public IWifiSniffer getModule() {
		return moduleInstance;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (!(obj instanceof EnabledWifiModule)){
			return false;
		}
		return moduleInstance.getName().equals(
				((EnabledWifiModule)obj).moduleInstance.getName());
	}
	
	@Override
	public int hashCode() {
		return moduleInstance.getName().hashCode();
	}
	
	@Override
	public String toString() {
		return moduleInstance == null ? "" : moduleInstance.getName();
	}
	
	private void startInterface(IWirelessCaptureInterface iface, long realm, final long spaceId) {
		final ISnifferHandle handle = sniffingEngine.createWifiHandle(iface, null, new IPacketSniffer<WiFiFrame>() {

			public void handlePacket(WiFiFrame packet, final IPacketContext ctx) {
				try {
					moduleInstance.handleWifiFrame(packet, new PacketModuleContext(ctx, spaceId));
				} catch (Exception e) {
					logger.warning("Exception processing wifi frame", e);
				}
				
			}
			
		});
		
		handle.setHighPriority();
		handle.setDefaultTag(new Long(realm));
		handle.register();
		activeHandles.add(handle);
	}
	
	public synchronized void start(Collection<WifiDaemonInterface> interfaces, long spaceId) {
		assert(activeHandles.isEmpty());
		assert(!running);
		
		for(WifiDaemonInterface iface : interfaces) {
			startInterface(iface.getInterface(), iface.getRealm(), spaceId);
		}
		running = true;
		
	}
	
	
	public synchronized void stop() {
		if(!running) return;
		for(ISnifferHandle handle : activeHandles) 
			handle.unregister();
		activeHandles.clear();
		running = false;
	}
}
