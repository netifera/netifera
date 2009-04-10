package com.netifera.platform.net.internal.pcap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.packets.decoders.EthernetDecoder;
import com.netifera.platform.net.packets.decoders.GenericDecoder;
import com.netifera.platform.net.packets.decoders.NullDecoder;
import com.netifera.platform.net.packets.decoders.RawIPDecoder;
import com.netifera.platform.net.pcap.Datalink;
import com.netifera.platform.net.pcap.IBPFProgram;
import com.netifera.platform.net.pcap.IPacketCapture;
import com.netifera.platform.net.pcap.IPacketHandler;

public class PacketCapture implements IPacketCapture, IPacketCaptureInternal {
	
	private final static IPacketDecoder ethernetDecoder = new EthernetDecoder();
	private final static IPacketDecoder nullDecoder = new NullDecoder();
	private final static IPacketDecoder rawipDecoder = new RawIPDecoder();
	
	private INativePacketCapture nativeCapture;
	private final IPacketHandler packetHandler;
	private final String interfaceName;
	private final int timeout;
	private final int snaplen;
	private final boolean promiscuous;
	private final List<Datalink> dltList = new ArrayList<Datalink>();
	private String errorMessage;
	private boolean testOnly;
	
	PacketCapture(String interfaceName, int snaplen, boolean promiscious, int timeout, IPacketHandler packetHandler) {
		this.interfaceName = interfaceName;
		this.snaplen = snaplen;
		this.promiscuous = promiscious;
		this.timeout = timeout;
		this.packetHandler = packetHandler;
	}
	
	PacketCapture(String interfaceName) {
		this(interfaceName, 0, false, 0, null);
		testOnly = true;
	}
	
	@Override
	public String toString() {
		return "(iface:" + interfaceName + ", snaplen:"
			+ snaplen + (promiscuous ? ", PROMISC)" : ")");
	}
	
	void setNativeCapture(INativePacketCapture nativeCapture) {
		this.nativeCapture = nativeCapture;
	}
	public boolean open() {
		if(nativeCapture == null) {
			throw new IllegalArgumentException("Native capture not initialized");
		}
		return nativeCapture.openLive(interfaceName, snaplen, timeout, promiscuous);
	}

	public boolean testOpen() {
		if(!open()) {
			return false;
		}
		close();
		return true;
	}
	public String getInterfaceName() {
		return interfaceName;
	}
	
	public int getSnaplen() {
		return snaplen;
	}
	
	public boolean isPromiscuous() {
		return promiscuous;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	public boolean read() {
		if(testOnly) {
			throw new IllegalStateException("Cannot read packets because capture was opened in 'test-only' mode");
		}
		return nativeCapture.packetRead(packetHandler);
	}
	
	public void close() {
		nativeCapture.close();
	}
	
	public void setError(String error) {
		errorMessage = error;		
	}

	public String getLastError() {
		return errorMessage;
	}
	public IBPFProgram createBPFProgram() {
		return new BPFProgram();
	}

	public Datalink getLinkType() {
		return nativeCapture.getLinkType();
	}

	public void dltListAdd(Datalink dlt) {
		if(dlt != null && dlt != Datalink.DLT_INVALID) {
			dltList.add(dlt);
		}
	}

	public void dltListClear() {
		dltList.clear();
	}
	
	public List<Datalink> getDltList() {
		return Collections.unmodifiableList(dltList);
	}
	
	
	public Datalink dltLookup(int n) {
		for(Datalink dlt : Datalink.values()) {
			if(dlt.getConstant() == n) {
				return dlt;
			}
		}
		return Datalink.DLT_INVALID;
	}

	public int getFileDescriptor() {
		return nativeCapture.getFileDescriptor();  
	}

	public boolean setDataLink(Datalink dlt) {
		return nativeCapture.setDataLink(dlt);
	}

	public IPacketDecoder getDecoder() {
		switch(nativeCapture.getLinkType()) {
		case DLT_EN10MB:
			return ethernetDecoder;
		case DLT_NULL:
			return nullDecoder;
		case DLT_RAW:
			return rawipDecoder;
		default:
			break;
		}
		
		return GenericDecoder.createForDatalink(nativeCapture.getLinkType().getConstant());
	}
}
