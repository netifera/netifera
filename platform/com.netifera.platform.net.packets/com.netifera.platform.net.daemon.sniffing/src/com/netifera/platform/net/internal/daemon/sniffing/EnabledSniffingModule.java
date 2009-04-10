package com.netifera.platform.net.internal.daemon.sniffing;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.daemon.sniffing.IArpSniffer;
import com.netifera.platform.net.daemon.sniffing.IIPSniffer;
import com.netifera.platform.net.daemon.sniffing.IRawSniffer;
import com.netifera.platform.net.daemon.sniffing.ISniffingModule;
import com.netifera.platform.net.daemon.sniffing.ITCPBlockSniffer;
import com.netifera.platform.net.daemon.sniffing.ITCPStreamSniffer;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.link.ARP;
import com.netifera.platform.net.packets.tcpip.IPv4;
import com.netifera.platform.net.packets.tcpip.IPv6;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.IPacketContext;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.ISnifferHandle;
import com.netifera.platform.net.sniffing.ISniffingEngineService;
import com.netifera.platform.net.sniffing.stream.IBlockSniffer;
import com.netifera.platform.net.sniffing.stream.IBlockSnifferHandle;
import com.netifera.platform.net.sniffing.stream.ISessionContext;

/**
 * This class wraps an instance of <code>ISniffingModule</code> which is enabled and
 * prepared to run (or actively running).  It allows registering a sniffing module on
 * multiple interfaces and manages the sniffing handles returned from the sniffing engine.
 */
class EnabledSniffingModule {
	
	/**
	 * Set containing handles returned from the sniffing engine.
	 */
	private final Set<ISnifferHandle> activeHandles = new HashSet<ISnifferHandle>();
	
	/**
	 * Flag indicating that the managed sniffing module is currently running
	 */
	private boolean running;
	
	/**
	 * Reference to the managed sniffing module instance.
	 */
	private final ISniffingModule moduleInstance;
	
	private final ISniffingModuleOutput output;
	
	private final ILogger logger;
	
	EnabledSniffingModule(ISniffingModule module, ISniffingModuleOutput output, ILogger logger) {
		this.moduleInstance = module;
		this.output = output;
		this.logger = logger;
	}
	
	public ISniffingModule getModule() {
		return moduleInstance;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (!(obj instanceof EnabledSniffingModule)){
			return false;
		}
		return moduleInstance.getName().equals(
				((EnabledSniffingModule)obj).moduleInstance.getName());
	}
	
	@Override
	public int hashCode() {
		return moduleInstance.getName().hashCode();
	}
	
	@Override
	public String toString() {
		return moduleInstance == null ? "" : moduleInstance.getName();
	}
	
	/**
	 * Start this module by creating sniffing handles for each type of packet handled
	 * by the module and on each listed interface.
	 * 
	 * @param interfaces Interfaces to sniff on.
	 */
	public synchronized void start(ISniffingEngineService sniffingEngine, Collection<SniffingDaemonInterface> interfaces, long spaceId) {
		assert(activeHandles.isEmpty());
		assert(!running);
		
		for(SniffingDaemonInterface iface : interfaces) {
			startInterface(sniffingEngine, iface.getInterface(), iface.getRealm(), spaceId);
		}
		running = true;
		
	}
	
	/*
	 * Start this module on a single interface.
	 */
	private void startInterface(ISniffingEngineService sniffingEngine, ICaptureInterface iface, long realm, long spaceId) {
		
		if(moduleInstance instanceof IRawSniffer) {
				register( createRawHandle(sniffingEngine, iface, (IRawSniffer) moduleInstance, spaceId), realm);
		}
		
		if(moduleInstance instanceof IIPSniffer) {
			register( createIPv4Handle(sniffingEngine, iface, (IIPSniffer) moduleInstance, spaceId), realm);
			register( createIPv6Handle(sniffingEngine, iface, (IIPSniffer) moduleInstance, spaceId), realm);
		}
		
		if(moduleInstance instanceof IArpSniffer) {
			register( createArpHandle(sniffingEngine, iface, (IArpSniffer) moduleInstance, spaceId), realm);
		}
		
		if(moduleInstance instanceof ITCPBlockSniffer) {
			register( createTcpBlockHandle(sniffingEngine, iface, (ITCPBlockSniffer) moduleInstance, spaceId), realm);
		}
		
		if(moduleInstance instanceof ITCPStreamSniffer) {
			register( createTcpStreamHandle(sniffingEngine, iface, (ITCPStreamSniffer) moduleInstance, spaceId), realm);
		}

	}
	
	/*
	 * Register a single handle, and save it in the set of active handles.
	 */
	private void register(ISnifferHandle handle, long realmId) {
		activeHandles.add(handle);
		handle.setDefaultTag(new Long(realmId));
		handle.register();
	}
	
	/*
	 * Create and return sniffing engine handle for receiving raw packets on the specified interface.
	 */
	private ISnifferHandle createRawHandle(ISniffingEngineService sniffingEngine, ICaptureInterface iface, final IRawSniffer sniffer, final long spaceId) {
		return sniffingEngine.createRawHandle(iface, moduleInstance.getFilter(),
				new IPacketSniffer<IPacketHeader>() {

					public void handlePacket(IPacketHeader packet,
							final IPacketContext ctx) {
						try {
							sniffer.handleRawPacket(packet, new PacketModuleContext(ctx, spaceId, output));
						} catch (Exception e) {
							logger.warning("Exception processing raw packet", e);
						}
					}
		});
		
	}
	
	/*
	 * Create and return a sniffing engine handle for receiving IPv4 packets on the specified interface.
	 */
	private ISnifferHandle createIPv4Handle(ISniffingEngineService sniffingEngine, ICaptureInterface iface, final IIPSniffer sniffer, final long spaceId) {
		return sniffingEngine.createIPv4Handle(iface, moduleInstance.getFilter(),
				new IPacketSniffer<IPv4>() {

					public void handlePacket(IPv4 packet, IPacketContext ctx) {
						try {
							sniffer.handleIPv4Packet(packet, new PacketModuleContext(ctx, spaceId, output));
						} catch (Exception e) {
							logger.warning("Exception processing IPv4 packet", e);
						}
					}
		});
	}
	
	/*
	 * Create and return a sniffing engine handle for receiving IPv6 packets on the specified interface.
	 */
	private ISnifferHandle createIPv6Handle(ISniffingEngineService sniffingEngine, ICaptureInterface iface, final IIPSniffer sniffer, final long spaceId) {
		return sniffingEngine.createIPv6Handle(iface, moduleInstance.getFilter(),
				new IPacketSniffer<IPv6>() {

					public void handlePacket(IPv6 packet, IPacketContext ctx) {
						try {
							sniffer.handleIPv6Packet(packet, new PacketModuleContext(ctx, spaceId, output));
						} catch (Exception e) {
							logger.warning("Exception processing IPv6 packet", e);
						}
					}
		});
	}
	
	/*
	 * Create and return a sniffing engine handle for receiving ARP packets on the specificed interface.
	 */
	private ISnifferHandle createArpHandle(ISniffingEngineService sniffingEngine, ICaptureInterface iface, final IArpSniffer sniffer, final long spaceId) {
		return sniffingEngine.createArpHandle(iface, moduleInstance.getFilter(),
				new IPacketSniffer<ARP>() {

					public void handlePacket(final ARP packet, final IPacketContext ctx) {
						try {
							sniffer.handleArpPacket(packet, new PacketModuleContext(ctx, spaceId, output));
						} catch (Exception e) {
							logger.warning("Exception processing ARP packet", e);
						}
					}
		});
	}
	
	private ISnifferHandle createTcpBlockHandle(ISniffingEngineService sniffingEngine, ICaptureInterface iface, final ITCPBlockSniffer sniffer, final long spaceId) {
		final IBlockSnifferHandle handle = sniffingEngine.createTcpBlockHandle(iface, moduleInstance.getFilter(), new IBlockSniffer() {

			public void handleBlock(final ISessionContext ctx, final ByteBuffer clientData,
					final ByteBuffer serverData) {
				try {
					sniffer.handleBlock(new StreamModuleContext(ctx, spaceId, output), clientData, serverData);
				} catch (Exception e) {
					logger.warning("Exception processing assembled TCP block", e);
				}
			}
			
		});
		
		sniffer.initialize(handle);
		
		return handle;
	}
	
	private ISnifferHandle createTcpStreamHandle(ISniffingEngineService sniffingEngine, ICaptureInterface iface, final ITCPStreamSniffer sniffer, long spaceId) {
			
		return sniffingEngine.createTcpStreamHandle(iface, moduleInstance.getFilter(),
				new StreamHandler(spaceId, sniffer, output));
		
	}
	/**
	 * Stop this module by unregistering all active sniffer engine handles.
	 */
	public synchronized void stop() {
		assert(running);
		for(ISnifferHandle handle : activeHandles) {
			handle.unregister();
		}
		activeHandles.clear();
		running = false;
	}
}
