package com.netifera.platform.net.sniffing.util;

import java.util.HashSet;
import java.util.Set;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.internal.sniffing.managers.IPacketManager;
import com.netifera.platform.net.internal.sniffing.managers.PacketContext;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.pcap.ICaptureHeader;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.IPacketContext;
import com.netifera.platform.net.sniffing.IPacketSnifferHandle;

public abstract class AbstractPacketManager<T extends IPacketHeader> 
	implements IPacketManager<T> {
	
	final private Set<IPacketSnifferHandle<T>> handles = new HashSet<IPacketSnifferHandle<T>>();
	final private Set<IPacketSnifferHandle<T>> priorityHandles = new HashSet<IPacketSnifferHandle<T>>();
	private final ICaptureInterface captureInterface;
	private final ISniffingEngineEx engine;

	private boolean disposed;
	private boolean started;
	
	protected AbstractPacketManager(ISniffingEngineEx engine, ICaptureInterface captureInterface) {
		this.engine = engine;
		this.captureInterface = captureInterface;
	}
	
	public ICaptureInterface getInterface() {
		return captureInterface;
	}
	
	public ISniffingEngineEx getSniffingEngine() {
		return engine;
	}
	
	public ILogger getLogger() {
		return engine.getLogger();
	}
	
	protected boolean isStarted() {
		return started;
	}
	
	protected boolean isDisposed() {
		return disposed;
	}
	
	protected boolean hasClients() {
		return !handles.isEmpty();
	}
	public synchronized void registerSniffer(IPacketSnifferHandle<T> handle, boolean priority) {
		if(disposed) {
			return;
		}
		if(!captureInterface.equals(handle.getInterface())) {
			throw new IllegalArgumentException("Mismatched interface");
		}
		
		if(handles.contains(handle) || priorityHandles.contains(handle)) {
			getLogger().warning("Ignoring duplicate sniffer handle registration");
			return;
		}
		
		if(priority) {
			priorityHandles.add(handle);
		} else {
			handles.add(handle);
		}
		
		if(!started) {
			if(start()) {
				started = true;
			}
		}
	}
	
	public void registerPrioritySniffer(IPacketSnifferHandle<T> handle) {
		registerSniffer(handle, true);
	}
	
	public synchronized void registerSniffer(IPacketSnifferHandle<T> handle) {
		registerSniffer(handle, false);
	}
	
	public synchronized void unregisterSniffer(IPacketSnifferHandle<T> handle) {
		if(disposed) {
			return;
		}
		
		if(!captureInterface.equals(handle.getInterface())) {
			throw new IllegalArgumentException("Mismatched interface");
		}
		
		if(!handles.contains(handle) && !priorityHandles.contains(handle)) {
			getLogger().warning("Attempt to unregister a handle which was not "
					+ "previously registered ignored");
			return;
		}
		
		handles.remove(handle);
		priorityHandles.remove(handle);
		
		if(handles.isEmpty() && priorityHandles.isEmpty() && started) {
			stop();
			started = false;
		}
	}
	

	protected void deliverPacket(T packet, ICaptureHeader header) {
		deliverPacket(packet, new PacketContext(header));
	}
	
	protected final synchronized void deliverPacket(T packet, IPacketContext ctx) {
		deliver(priorityHandles, packet, ctx);
		deliver(handles, packet, ctx);
	}
	
	private void deliver(Set<IPacketSnifferHandle<T>> handleSet, T packet, IPacketContext ctx) {
		for(IPacketSnifferHandle<T> handle : handleSet) {
			final Object tag = handle.getDefaultTag();
			if(tag != null && ctx.getPacketTag() == null) ctx.setPacketTag(tag);
			handle.getSniffer().handlePacket(packet, ctx);
			if(ctx.isAborted())
				return;
		}
	}
	
	abstract protected boolean start();
	
	abstract protected void stop();
}
