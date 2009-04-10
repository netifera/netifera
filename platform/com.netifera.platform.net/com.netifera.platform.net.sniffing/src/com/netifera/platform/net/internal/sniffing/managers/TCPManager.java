package com.netifera.platform.net.internal.sniffing.managers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.internal.sniffing.stream.TCPReassemblyConfig;
import com.netifera.platform.net.internal.sniffing.stream.TCPSession;
import com.netifera.platform.net.internal.sniffing.stream.TCPSessionKey;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.tcpip.IP;
import com.netifera.platform.net.packets.tcpip.IPv4;
import com.netifera.platform.net.packets.tcpip.IPv6;
import com.netifera.platform.net.packets.tcpip.TCP;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.IPacketContext;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.IPacketSniffer;
import com.netifera.platform.net.sniffing.IPacketSnifferHandle;
import com.netifera.platform.net.sniffing.ISnifferHandle;
import com.netifera.platform.net.sniffing.stream.IBlockSnifferHandle;
import com.netifera.platform.net.sniffing.stream.IStreamSnifferHandle;
import com.netifera.platform.net.sniffing.util.ISniffingEngineEx;

public class TCPManager {
	
	private final Set<IStreamSnifferHandle> streamHandles =
		Collections.synchronizedSet( new HashSet<IStreamSnifferHandle>());
	
	private final Set <IBlockSnifferHandle> blockHandles =
		Collections.synchronizedSet( new HashSet<IBlockSnifferHandle>());
		
	/* Currently active sessions that we are tracking */
	private final Map<TCPSessionKey, TCPSession> sessionMap =
		new ConcurrentHashMap<TCPSessionKey, TCPSession>();
	
	/* Sniffer handle for receiving incoming IP level traffic */
	private final IPacketSnifferHandle<IPv4> ipv4Handle;
	private final IPacketSnifferHandle<IPv6> ipv6Handle;
	
	/* The interface this manager belongs to */
	private final ICaptureInterface captureInterface;
	
	/* The sniffing engine this manager belongs to */
	private final ISniffingEngineEx engine;

	private final TCPReassemblyConfig config;
	/* When BPF is finally working, we'll use this */
	private final IPacketFilter filter = null;
	
	private final ILogger logger;
	
	private boolean disposed;
	private boolean started;
	
	public TCPManager(IPacketManager<IPacketHeader> packetManager,
			IPv4Manager ipv4Manager, IPv6Manager ipv6Manager) {
		this.engine = packetManager.getSniffingEngine();
		logger = engine.getLogger();
		if(System.getProperty("netifera.debug.tcp") != null){
			logger.enableDebug();
		}
		this.captureInterface = packetManager.getInterface();
		
		ipv4Handle = ipv4Manager == null ? null :
			new SnifferHandle<IPv4>(ipv4Manager, filter,
				new IPacketSniffer<IPv4>() {

					public void handlePacket(IPv4 packet, IPacketContext ctx) {
						handleIP(packet, ctx);
					}
		});
		ipv6Handle = ipv6Manager == null ? null :
			new SnifferHandle<IPv6>(ipv6Manager, filter,
				new IPacketSniffer<IPv6>() {

					public void handlePacket(IPv6 packet, IPacketContext ctx) {
						handleIP(packet, ctx);
					}
		});
		
		config = new TCPReassemblyConfig();
	}

	ISniffingEngineEx getSniffingEngine() {
		return engine;
	}
	
	ICaptureInterface getInterface() {
		return captureInterface;
	}
	
	/*
	 * Called when IP packets arrive.
	 */
	private void handleIP(IP ip, IPacketContext ctx) {
		final TCP tcp = (TCP) ip.findHeader(TCP.class);
		if(tcp == null) {
			return;
		}
		final TCPSessionKey key = new TCPSessionKey(ip, tcp);
		final long currentTimestamp = ctx.getCaptureHeader().getSeconds();
		assert(currentTimestamp != 0);
		/*
		 * If a session does not already exist for this key,
		 * and this is a SYN packet, create a new session.
		 */
		if(!sessionMap.containsKey(key)) {

			/* ignore the packet if isn't part of a session and SYN isn't set */
			if(!tcp.getSYN()) {
				return;
			}

			if(!isBelowSessionLimit()) {
				logger.warning("Failed to create TCPSession because limit of " + config.getMaximumSessionCount()
						+ " sessions has been reached");
				return;
			}
			
			createSession(key, currentTimestamp, ctx.getPacketTag());
		}

		assert(sessionMap.containsKey(key));
		
		final TCPSession session = sessionMap.get(key);
		
		if(session.addSegment(tcp, currentTimestamp)) {
			logger.debug("Session closed normally for " + key + " (" + sessionMap.size() + ")");
			sessionMap.remove(key);

		}
		
		processSessionTimeouts(currentTimestamp);
	}
	
	private void processSessionTimeouts(long currentTimestamp) {
		for(Iterator<Entry<TCPSessionKey, TCPSession>> iter = sessionMap.entrySet().iterator(); iter.hasNext(); ) {
			TCPSession session = iter.next().getValue();
			if(session.isClosedOnTimeout(currentTimestamp)) {
				iter.remove();
			}
		}
	}
	
	private boolean isBelowSessionLimit() {
		return sessionMap.size() < config.getMaximumSessionCount();
	}
	
	private synchronized void createSession(TCPSessionKey key, long timestamp, Object packetTag) {
		sessionMap.put(key, new TCPSession(key, config, logger, timestamp, packetTag, streamHandles, blockHandles));
		logger.debug("Session added for " + key + " (" + sessionMap.size() + ")");
	}
	
	/* Start manager and begin receiving packets from IP layer */
	private void start() {
		if(started) {
			return;
		}
		if (ipv4Handle != null) {
			ipv4Handle.register();
		}
		if (ipv6Handle != null) {
			ipv6Handle.register();
		}
		started = true;
	}
	
	/* Stop manager and stop receiving packets from IP layer */
	private void stop() {
		if(!started) {
			return;
		}
		if (ipv4Handle != null) {
			ipv4Handle.unregister();
		}
		if (ipv6Handle != null) {
			ipv6Handle.unregister();
		}
		started = false;
	}
	
	
	public synchronized void registerStreamHandle(IStreamSnifferHandle handle) {
		if(canRegisterHandle(handle, streamHandles)) {
			streamHandles.add(handle);
			if(!started) {
				start();
			}
		}
	}
	
	public synchronized void registerBlockHandle(IBlockSnifferHandle handle) {
		if(canRegisterHandle(handle, blockHandles)) {
			blockHandles.add(handle);
			if(!started) {
				start();
			}
		}
	}
	
	public synchronized void unregisterStreamHandle(IStreamSnifferHandle handle) {
		if(!unregisterHandle(handle, streamHandles)) {
			return;
		}
		
		for(TCPSession session : sessionMap.values()) {
			session.unregisterStreamHandle(handle);
		}
	}
	
	public synchronized void unregisterBlockHandle(IBlockSnifferHandle handle) {
		if(!unregisterHandle(handle, blockHandles)) {
			return;
		}
		
		for(TCPSession session : sessionMap.values()) {
			session.unregisterBlockHandle(handle);
		}
	}
	
	private boolean canRegisterHandle(ISnifferHandle handle, Set<? extends ISnifferHandle> set) {
		if(disposed) {
			return false;
		}
		
		if(!captureInterface.equals(handle.getInterface())) {
			throw new IllegalArgumentException("Mismatched interface registering handle");
		}
		
		if(set.contains(handle)) {
			logger.warning("Ignoring duplicate registration of sniffer handle");
			return false;
		}
		
		return true;
	}
	
	private boolean unregisterHandle(ISnifferHandle handle, Set<? extends ISnifferHandle> set) {
		if(disposed) {
			return false;
		}
		if(!captureInterface.equals(handle.getInterface())) {
			throw new IllegalArgumentException("Mismatched interface unregistering handle");
		}
		if(!set.contains(handle)) {
			logger.warning("Ignoring attempt to unregister a handle which was not previously registered");
			return false;
		}
		
		set.remove(handle);
		
		if(streamHandles.isEmpty() && blockHandles.isEmpty() && started) {
			stop();
		}
		
		return true;
	}
}
