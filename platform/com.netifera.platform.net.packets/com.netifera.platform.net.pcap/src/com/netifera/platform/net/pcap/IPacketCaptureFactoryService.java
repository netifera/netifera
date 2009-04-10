package com.netifera.platform.net.pcap;

import java.util.Collection;

public interface IPacketCaptureFactoryService {
	
	
	Collection<ICaptureInterface> getInterfaces();
	/**
	 * The difference between this method and getInterfaces() is that
	 * this method polls the system to refresh the information about
	 * which interfaces are available.
	 * @return
	 */
	Collection<ICaptureInterface> getCurrentInterfaces();

	IPacketCapture create(ICaptureInterface iface, int snaplen, boolean promiscuous,
			int timeout, IPacketHandler packetHandler);

}
