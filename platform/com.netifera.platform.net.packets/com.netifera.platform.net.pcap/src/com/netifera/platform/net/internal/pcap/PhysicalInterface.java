package com.netifera.platform.net.internal.pcap;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import com.netifera.platform.net.pcap.IPacketCapture;
import com.netifera.platform.net.pcap.IPacketCaptureFactoryService;
import com.netifera.platform.net.pcap.IPacketHandler;
import com.netifera.platform.net.pcap.ex.ICaptureInterfaceEx;

public class PhysicalInterface implements ICaptureInterfaceEx {
	private final IPacketCaptureFactoryService factory;
	private final NetworkInterface interfaceInstance;
	private boolean canCapture;
	
	PhysicalInterface(IPacketCaptureFactoryService factory, NetworkInterface iface,  boolean canCapture) {
		this.factory = factory;
		this.interfaceInstance = iface;
		this.canCapture = canCapture;
	}
	
	public NetworkInterface getInterface() {
		return interfaceInstance;
	}
	
	public String getName() {
		return interfaceInstance.getDisplayName();
	}
	
	public boolean captureAvailable() {
		return canCapture;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getName());
		Enumeration<InetAddress> addresses = interfaceInstance.getInetAddresses();
		if(addresses.hasMoreElements()) {
			buffer.append(" (");
			while (addresses.hasMoreElements()) {
				buffer.append(addresses.nextElement().getHostAddress());
				if (addresses.hasMoreElements())
					buffer.append(", ");
			}
			buffer.append(')');
		}
		return buffer.toString();
	}
	
	public boolean equals(Object other) {
		if(!(other instanceof PhysicalInterface))
			return false;
		else
			return interfaceInstance.equals(((PhysicalInterface)other).interfaceInstance);
	}
	
	public int hashCode() {
		return interfaceInstance.hashCode();
	}

	public IPacketCapture pcapCreate(int snaplen, boolean promiscuous,
			int timeout, IPacketHandler handler) {
	
		return factory.create(this, snaplen, promiscuous, timeout, handler);
	}
}
