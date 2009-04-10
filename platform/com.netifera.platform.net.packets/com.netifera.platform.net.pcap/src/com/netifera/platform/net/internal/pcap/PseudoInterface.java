package com.netifera.platform.net.internal.pcap;

import com.netifera.platform.net.pcap.IPacketCapture;
import com.netifera.platform.net.pcap.IPacketCaptureFactoryService;
import com.netifera.platform.net.pcap.IPacketHandler;
import com.netifera.platform.net.pcap.ex.ICaptureInterfaceEx;

public class PseudoInterface implements ICaptureInterfaceEx {

	private final IPacketCaptureFactoryService factory;
	private final String name;
	PseudoInterface(IPacketCaptureFactoryService factory, String name) {
		this.factory = factory;
		this.name = name;
	}
	public IPacketCapture pcapCreate(int snaplen, boolean promiscuous,
			int timeout, IPacketHandler handler) {
		return factory.create(this, snaplen, promiscuous, timeout, handler);
	}

	public boolean equals(Object other) {
		if(!(other instanceof PseudoInterface))
			return false;
		else
			return name.equals(((PseudoInterface)other).name);
	}
	
	public int hashCode() {
		return name.hashCode();
	}

	public String toString() {
		return name;
	}
	public boolean captureAvailable() {
		return true;
	}

	public String getName() {
		return name;
	}
	

}
